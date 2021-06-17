# Face_recognition
安卓端人脸识别，蓝牙传输

----
### 大致功能
- 开机之后从远端下载本机器对应的人脸图片，初步使用账号密码确定登陆的设备
- 识别出结果后，将结果同步返回到远程数据库，同时使用蓝牙发送信号

### 大致步骤
1. 登陆
    - 检查本地是否存有token，如果有，直接跳过登陆界面
    - 返回该账号token，存储在本地。
2. 同步人脸数据
    - 建立本地数据库，避免每次开机都要重新初始化
    - 使用哈希值判断图片是否有更新
3. 主页面
    - 蓝牙连接
    - 正常识别
    - 设置
4. 蓝牙连接
    - 可以跳过连接
5. 正常启动
6. 结果同步蓝牙传输本地设备与网络数据库
    - 网络返回数据时候携带token
7. 设置页面
    - 包括重新初始化数据库
    - 设置识别度
    - 蓝牙重新连接
    - 退出登陆
8. 记录页面
    - 查看目前本地数据
    - 查看识别结果记录
    
### 数据表
本地数据库
- image
	| Id | image_id | name | fea  | hashcode |
	| ----------------- | ---- | -------- | ---- | ---- |
	| 字段id值(自增) | 图片对应远端数据库的id(主要用这个字段判断获取的人脸) | 名字 | 特征值  | 哈希值(用来判断图片是否更新) |

### 模块

##### 登陆
- 两个输入框(输入账号和密码), 一个按钮(点击登陆)
- 网络请求获取token，存储在本地`sharePreferences`中
##### 同步人脸数据
1. 从网络获取本账号对应的图片的id以及hashcode
2. 和本地数据库比对，拿出对应id，比对hashcode
    1. hashcode相等，把该字段对应的name和fea拿出来保存在map中
    2. hashcode不等，或者找不到该id，网络请求获取该id对应的图片到本地进行初始化
3. 等待该账号下所有需要同步的照片同步完毕，进入主页面
##### 主页面
提示蓝牙是否连接
展示三个btn：
- 进行蓝牙连接
- 进行识别
- 进行设置
##### 蓝牙连接
- 获取蓝牙权限
- 使用RecyclerView或者ListView展示搜索到的蓝牙设备
- 用户点击开始连接
##### 正常启动识别
- 获取摄像头权限
- 进入正常识别流程
- 结果展示(展示过程中进行蓝牙传输和网络请求)
##### 结果传输
- `Retrofit`或者`Okhttp`发送与请求网络接口
##### 页面设置(需要进行管理员身份认证)
- 本地列表
    - 用户可以向左滑删除该用户数据(同步网络删除请求)
- 识别记录
    - 默认展示该设备所有记录
    - 用户可以选择成功列表或者陌生人列表以及日期