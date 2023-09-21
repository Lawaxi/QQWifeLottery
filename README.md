# QQWifeLottery

Mirai-Console插件，构建mirai（[mamoe/mirai](https://github.com/mamoe/mirai), [docs.mirai.mamoe.net](https://docs.mirai.mamoe.net/)）后拖入plugins文件夹运行即可，首次运行生成配置

- 本插件每次可抽一位群友做老婆
- 每天（八点刷新）首次抽可换，之后CD两小时

*请下载release中普通版，文件名为wifeOttery-xxx.mirai2.jar*

### SNH48 Group版

- 本插件每次可抽一位SNH48 Group成员做老婆
- 有新成员出道时，在任意群中发送`update_star_data`更新本地数据
- 通过配置项allowGroups可选择在哪些群中开启此功能，以逗号隔开群号
- 通过配置项birthdayBroadcastGroup可选择在哪些群中开启生日祝福，以逗号隔开群号

*请下载release中48ver版，文件名为wifeOttery48-xxx.mirai2.jar*

#### 生日祝福[>=0.1.9-test3]

将自动在每天零点为star_data中所有今日生日小偶像送上祝福，自动由公式照生成一张生日海报

配置项birthdayBroadcastFont为海报文字字体，默认Microsoft YaHei，请确认系统中包含填写的字体

##### Linux安装字体

- 将字体文件放入/usr/share/fonts目录
- `fc-cache -f -v` 更新字体
- `fc-list :lang=zh` 当前系统中中文字体

### 配置示例

配置用于自定义事项，一般无需更改

如果想将“老婆”换为“老公”，可以将配置改为

~~~yaml
[system]
lottery = 来个老公,换个老公
data = 我的老公
lotteryOut = 今日老公：%s | 情愫：%d%% %s\n%s后可更换
dataOut = \n累计带走%d人 共%d次\n带走次数御三：%s
dataVoidOut = \n你还没有老公~ 情愫达到80%才可以带走捏
~~~
