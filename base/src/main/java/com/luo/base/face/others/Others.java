//package com.luo.base.face.others;
//
//public class Others {
//    public static float evaluate(float[] fea1, float[] fea2) {
//        float dist = 0;
//        for (int i = 0; i < 128; i++) {
//            dist += Math.pow(fea1[i] - fea2[i], 2);
//        }
//        float same = 0;
//        for (int i = 0; i < 400; i++) {
//            float threshold = 0.01f * (i + 1);
//            if (dist < threshold) {
//                same += 1.0 / 400;
//            }
//        }
//        return same;
//    }
//}
