#include <stdio.h>
#include <vector>
#include <algorithm>
#include "retinaface.hpp"


static inline float intersection_area(const FaceObject &a, const FaceObject &b) {
    return Rect<float>::intersection_area(a.rect, b.rect);
}

static void qsort_descent_inplace(std::vector <FaceObject> &faceobjects, int left, int right) {
    int i = left;
    int j = right;
    float p = faceobjects[(left + right) / 2].prob;

    while (i <= j) {
        while (faceobjects[i].prob > p)
            i++;

        while (faceobjects[j].prob < p)
            j--;

        if (i <= j) {
            // swap
            std::swap(faceobjects[i], faceobjects[j]);

            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(faceobjects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(faceobjects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector <FaceObject> &faceobjects) {
    if (faceobjects.empty())
        return;

    qsort_descent_inplace(faceobjects, 0, faceobjects.size() - 1);
}

static void
nms_sorted_bboxes(const std::vector <FaceObject> &faceobjects, std::vector<int> &picked,
                  float nms_threshold) {
    picked.clear();

    const int n = faceobjects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++) {
        areas[i] = faceobjects[i].rect.area();
    }

    for (int i = 0; i < n; i++) {
        const FaceObject &a = faceobjects[i];

        int keep = 1;
        for (int j = 0; j < (int) picked.size(); j++) {
            const FaceObject &b = faceobjects[picked[j]];

            // intersection over union
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[picked[j]] - inter_area;
            //             float IoU = inter_area / union_area
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.push_back(i);
    }
}

// copy from src/layer/proposal.cpp
static ncnn::Mat generate_anchors(int base_size, const ncnn::Mat &ratios, const ncnn::Mat &scales) {
    int num_ratio = ratios.w;
    int num_scale = scales.w;

    ncnn::Mat anchors;
    anchors.create(4, num_ratio * num_scale);

    const float cx = base_size * 0.5f;
    const float cy = base_size * 0.5f;

    for (int i = 0; i < num_ratio; i++) {
        float ar = ratios[i];

        int r_w = round(base_size / sqrt(ar));
        int r_h = round(r_w * ar); //round(base_size * sqrt(ar));

        for (int j = 0; j < num_scale; j++) {
            float scale = scales[j];

            float rs_w = r_w * scale;
            float rs_h = r_h * scale;

            float *anchor = anchors.row(i * num_scale + j);

            anchor[0] = cx - rs_w * 0.5f;
            anchor[1] = cy - rs_h * 0.5f;
            anchor[2] = cx + rs_w * 0.5f;
            anchor[3] = cy + rs_h * 0.5f;
        }
    }

    return anchors;
}

static void
generate_proposals(const ncnn::Mat &anchors, int feat_stride, const ncnn::Mat &score_blob,
                   const ncnn::Mat &bbox_blob,
                   const ncnn::Mat &landmark_blob, float prob_threshold,
                   std::vector <FaceObject> &faceobjects) {
    int w = score_blob.w;
    int h = score_blob.h;

    // generate face proposal from bbox deltas and shifted anchors
    const int num_anchors = anchors.h;

    for (int q = 0; q < num_anchors; q++) {
        const float *anchor = anchors.row(q);

        const ncnn::Mat score = score_blob.channel(q + num_anchors);
        const ncnn::Mat bbox = bbox_blob.channel_range(q * 4, 4);
        const ncnn::Mat landmark = landmark_blob.channel_range(q * 10, 10);

        // shifted anchor
        float anchor_y = anchor[1];

        float anchor_w = anchor[2] - anchor[0];
        float anchor_h = anchor[3] - anchor[1];

        for (int i = 0; i < h; i++) {
            float anchor_x = anchor[0];

            for (int j = 0; j < w; j++) {
                int index = i * w + j;

                float prob = score[index];

                if (prob >= prob_threshold) {
                    // apply center size
                    float dx = bbox.channel(0)[index];
                    float dy = bbox.channel(1)[index];
                    float dw = bbox.channel(2)[index];
                    float dh = bbox.channel(3)[index];

                    float cx = anchor_x + anchor_w * 0.5f;
                    float cy = anchor_y + anchor_h * 0.5f;

                    float pb_cx = cx + anchor_w * dx;
                    float pb_cy = cy + anchor_h * dy;

                    float pb_w = anchor_w * exp(dw);
                    float pb_h = anchor_h * exp(dh);

                    float x0 = pb_cx - pb_w * 0.5f;
                    float y0 = pb_cy - pb_h * 0.5f;
                    float x1 = pb_cx + pb_w * 0.5f;
                    float y1 = pb_cy + pb_h * 0.5f;

                    FaceObject obj;
                    obj.rect.x = x0;
                    obj.rect.y = y0;
                    obj.rect.width = x1 - x0 + 1;
                    obj.rect.height = y1 - y0 + 1;
                    obj.landmark[0].x = cx + (anchor_w + 1) * landmark.channel(0)[index];
                    obj.landmark[0].y = cy + (anchor_h + 1) * landmark.channel(1)[index];
                    obj.landmark[1].x = cx + (anchor_w + 1) * landmark.channel(2)[index];
                    obj.landmark[1].y = cy + (anchor_h + 1) * landmark.channel(3)[index];
                    obj.landmark[2].x = cx + (anchor_w + 1) * landmark.channel(4)[index];
                    obj.landmark[2].y = cy + (anchor_h + 1) * landmark.channel(5)[index];
                    obj.landmark[3].x = cx + (anchor_w + 1) * landmark.channel(6)[index];
                    obj.landmark[3].y = cy + (anchor_h + 1) * landmark.channel(7)[index];
                    obj.landmark[4].x = cx + (anchor_w + 1) * landmark.channel(8)[index];
                    obj.landmark[4].y = cy + (anchor_h + 1) * landmark.channel(9)[index];
                    obj.prob = prob;

                    faceobjects.push_back(obj);
                }

                anchor_x += feat_stride;
            }

            anchor_y += feat_stride;
        }
    }
}

std::vector <FaceObject> detect_retinaface(const ncnn::Net &retinaface, ncnn::Mat in) {

//    ncnn::Extractor ex = retinaface.create_extractor();
//    ex.set_light_mode(true);
//    ex.set_num_threads(4);
//    ex.input(0, in);
//    ncnn::Mat out, out1, out2;
//
//    // loc
//    ex.extract("output0", out);
//
//    // class
//    ex.extract("530", out1);
//
//    //landmark
//    ex.extract("529", out2);
//    std::vector<box> anchor;
//    if (false)
//        create_anchor_retinaface(anchor,  bgr.cols, bgr.rows);
//    else
//        create_anchor(anchor,  bgr.cols, bgr.rows);
//
//    std::vector<bbox > total_box;
//    float *ptr = out.channel(0);
//    float *ptr1 = out1.channel(0);
//    float *landms = out2.channel(0);
//
//    // #pragma omp parallel for num_threads(2)
//    for (int i = 0; i < anchor.size(); ++i)
//    {
//        if (*(ptr1+1) > 0.6)
//        {
//            box tmp = anchor[i];
//            box tmp1;
//            bbox result;
//
//            // loc and conf
//            tmp1.cx = tmp.cx + *ptr * 0.1 * tmp.sx;
//            tmp1.cy = tmp.cy + *(ptr+1) * 0.1 * tmp.sy;
//            tmp1.sx = tmp.sx * exp(*(ptr+2) * 0.2);
//            tmp1.sy = tmp.sy * exp(*(ptr+3) * 0.2);
//
//            result.x1 = (tmp1.cx - tmp1.sx/2) * in.w;
//            if (result.x1<0)
//                result.x1 = 0;
//            result.y1 = (tmp1.cy - tmp1.sy/2) * in.h;
//            if (result.y1<0)
//                result.y1 = 0;
//            result.x2 = (tmp1.cx + tmp1.sx/2) * in.w;
//            if (result.x2>in.w)
//                result.x2 = in.w;
//            result.y2 = (tmp1.cy + tmp1.sy/2)* in.h;
//            if (result.y2>in.h)
//                result.y2 = in.h;
//            result.s = *(ptr1 + 1);
//
//            // landmark
//            for (int j = 0; j < 5; ++j)
//            {
//                result.point[j]._x =( tmp.cx + *(landms + (j<<1)) * 0.1 * tmp.sx ) * in.w;
//                result.point[j]._y =( tmp.cy + *(landms + (j<<1) + 1) * 0.1 * tmp.sy ) * in.h;
//            }
//
//            total_box.push_back(result);
//        }
//        ptr += 4;
//        ptr1 += 2;
//        landms += 10;
//    }
//
//    std::sort(total_box.begin(), total_box.end(), cmp);
//    nms(total_box, 0.4);
//    printf("%d\n", (int)total_box.size());
//
//    for (int j = 0; j < total_box.size(); ++j)
//    {
//        boxes.push_back(total_box[j]);
//    }

    const float prob_threshold = 0.8f;
    const float nms_threshold = 0.4f;


    int img_w = in.w;
    int img_h = in.h;

    ncnn::Extractor ex = retinaface.create_extractor();
    ex.set_num_threads(4 );
    ex.set_vulkan_compute(true);

    ex.input("data", in);

    std::vector <FaceObject> faceproposals;

    // stride 32
    {
        ncnn::Mat score_blob, bbox_blob, landmark_blob;
        ex.extract("face_rpn_cls_prob_reshape_stride32", score_blob);
        ex.extract("face_rpn_bbox_pred_stride32", bbox_blob);
        ex.extract("face_rpn_landmark_pred_stride32", landmark_blob);

        const int base_size = 16;
        const int feat_stride = 32;
        ncnn::Mat ratios(1);
        ratios[0] = 1.f;
        ncnn::Mat scales(2);
        scales[0] = 32.f;
        scales[1] = 16.f;
        ncnn::Mat anchors = generate_anchors(base_size, ratios, scales);

        std::vector <FaceObject> faceobjects32;
        generate_proposals(anchors, feat_stride, score_blob, bbox_blob, landmark_blob,
                           prob_threshold, faceobjects32);

        faceproposals.insert(faceproposals.end(), faceobjects32.begin(), faceobjects32.end());
    }

    // stride 16
    {
        ncnn::Mat score_blob, bbox_blob, landmark_blob;
        ex.extract("face_rpn_cls_prob_reshape_stride16", score_blob);
        ex.extract("face_rpn_bbox_pred_stride16", bbox_blob);
        ex.extract("face_rpn_landmark_pred_stride16", landmark_blob);

        const int base_size = 16;
        const int feat_stride = 16;
        ncnn::Mat ratios(1);
        ratios[0] = 1.f;
        ncnn::Mat scales(2);
        scales[0] = 8.f;
        scales[1] = 4.f;
        ncnn::Mat anchors = generate_anchors(base_size, ratios, scales);

        std::vector <FaceObject> faceobjects16;
        generate_proposals(anchors, feat_stride, score_blob, bbox_blob, landmark_blob,
                           prob_threshold, faceobjects16);

        faceproposals.insert(faceproposals.end(), faceobjects16.begin(), faceobjects16.end());
    }

    // stride 8
    {
        ncnn::Mat score_blob, bbox_blob, landmark_blob;
        ex.extract("face_rpn_cls_prob_reshape_stride8", score_blob);
        ex.extract("face_rpn_bbox_pred_stride8", bbox_blob);
        ex.extract("face_rpn_landmark_pred_stride8", landmark_blob);

        const int base_size = 16;
        const int feat_stride = 8;
        ncnn::Mat ratios(1);
        ratios[0] = 1.f;
        ncnn::Mat scales(2);
        scales[0] = 2.f;
        scales[1] = 1.f;
        ncnn::Mat anchors = generate_anchors(base_size, ratios, scales);

        std::vector <FaceObject> faceobjects8;
        generate_proposals(anchors, feat_stride, score_blob, bbox_blob, landmark_blob,
                           prob_threshold, faceobjects8);

        faceproposals.insert(faceproposals.end(), faceobjects8.begin(), faceobjects8.end());
    }

    // sort all proposals by score from highest to lowest
    qsort_descent_inplace(faceproposals);

    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(faceproposals, picked, nms_threshold);

    int face_count = picked.size();
    std::vector <FaceObject> faceobjects(face_count);

    for (int i = 0; i < face_count; i++) {
        faceobjects[i] = faceproposals[picked[i]];

        // clip to image size
        float x0 = faceobjects[i].rect.x;
        float y0 = faceobjects[i].rect.y;
        float x1 = x0 + faceobjects[i].rect.width;
        float y1 = y0 + faceobjects[i].rect.height;

        x0 = std::max(std::min(x0, (float) img_w - 1), 0.f);
        y0 = std::max(std::min(y0, (float) img_h - 1), 0.f);
        x1 = std::max(std::min(x1, (float) img_w - 1), 0.f);
        y1 = std::max(std::min(y1, (float) img_h - 1), 0.f);

        faceobjects[i].rect.x = x0;
        faceobjects[i].rect.y = y0;
        faceobjects[i].rect.width = x1 - x0;
        faceobjects[i].rect.height = y1 - y0;
    }

    return faceobjects;
}


void refineAndSquareBbox(std::vector<Bbox> &vecBbox, const int &height, const int &width) {
    if (vecBbox.empty()) {
        return;
    }
    float bbw = 0, bbh = 0, maxSide = 0;
    float h = 0, w = 0;
    float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
    for (std::vector<Bbox>::iterator it = vecBbox.begin(); it != vecBbox.end(); it++) {
        if ((*it).exist) {
            bbw = (*it).x2 - (*it).x1 + 1;
            bbh = (*it).y2 - (*it).y1 + 1;
            x1 = (*it).x1 + (*it).regreCoord[0] * bbw;
            y1 = (*it).y1 + (*it).regreCoord[1] * bbh;
            x2 = (*it).x2 + (*it).regreCoord[2] * bbw;
            y2 = (*it).y2 + (*it).regreCoord[3] * bbh;

            w = x2 - x1 + 1;
            h = y2 - y1 + 1;

            maxSide = (h > w) ? h : w;
            x1 = x1 + w * 0.5 - maxSide * 0.5;
            y1 = y1 + h * 0.5 - maxSide * 0.5;
            (*it).x2 = round(x1 + maxSide - 1);
            (*it).y2 = round(y1 + maxSide - 1);
            (*it).x1 = round(x1);
            (*it).y1 = round(y1);

            //boundary check
            if ((*it).x1 < 0)(*it).x1 = 0;
            if ((*it).y1 < 0)(*it).y1 = 0;
            if ((*it).x2 > width)(*it).x2 = width - 1;
            if ((*it).y2 > height)(*it).y2 = height - 1;

            it->area = (it->x2 - it->x1) * (it->y2 - it->y1);
        }
    }
}

