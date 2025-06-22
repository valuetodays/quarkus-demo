# valuetodays-trade-etf

## 前置条件

- java17
- maven3.6.3
- （自2024-04-01开始不再需要操作win32窗口了）~~https://www.htsc.com.cn/site-services/downloads
  （如下是我使用的版本，一般需要最新版本）~~
  + ~~网上交易系统（专业版Ⅲ）V6.18 （可以看行情，功能较强大）~~
  + ~~独立下单系统V6.18.17 （建议使用它）~~
    - ~~启动前去安装目录，把lct目录改名为lct1（它是另外一个界面，并不需要启动）~~
    - ~~修改设置~~
- e海方舟-量化交易版

## 运行

- idea中，运行 cn.valuetodays.trade.MainTradeTest#doTrade()
- 命令行 `mvn test -DskipUnitTest=false -Dtest=cn/valuetodays/trade/MainTradeTest#doTrade`

## 目标

## todo:

- [x] buy
  - [x] ~~buy by click window~~
  - [x] by http request
- [x] sell
  - [x] ~~by click window~~
  - [x] by http request
- [x] get available balance (获取可用余额 / 可取金额 / 冻结金额 / 股票市值金额 / 总资产 等信息)
  - [x] ~~by click window~~
  - [ ] by http request
- [x] get buy/sell strategy buy http api
- [ ] ~~获取 资源id为0x81的SysTreeView32的数据，并触发买入/卖出操作~~
- [x] ~~获取 资源id为0xe800 的 ToolBar32 的数据，并触发买入/卖出操作~~
  + [x] 见cn.valuetodays.win32.Win32Utils.clickWindow()
- [x] 使用yaml来做属性配置
- [x] 写篇博客
- [x] 使用logger
- [x] ~~获取持仓~~
  + [x] 手动ctrl+c复制出数据，再使用程序识别验证码，验证后持仓列表就在剪贴板中
  + [x] 操作剪贴板
  + [x] 处理验证码
    + [x] tesseract-ocr-w64-setup-5.3.1.20230401.exe
    + Please make sure the TESSDATA_PREFIX environment variable is set to your "tessdata" directory.
  + [x] 将持仓同步到mysql中。见cn.valuetodays.trade.BaseAutoTrade.parseHolderAndSaveToMysql()
- [ ] 开虚拟机运行交易软件是否可行，这样的话，就不影响用电脑了

在开始之前，请对客户端调整以下设置，不然会导致下单时价格出错以及客户端超时锁定。

系统设置 > 界面设置: 界面不操作超时时间设为 0
系统设置 > 交易设置: 默认买入价格/买入数量/卖出价格/卖出数量 都设置为 空

同时客户端不能最小化也不能处于精简模式。

### 参考

http://www.rgagnon.com/topics/java-jni.html 这个网站上有几个JNA的实例，熟悉Windows窗体编程的朋友们看起来应该很容易。
https://github.com/java-native-access/jna#readme
http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html 这个是新加坡理工大学的网站，想入门JNI的可以去看看。
http://stackoverflow.com/questions/28538234/sending-a-keyboard-input-with-java-jna-and-sendinput 包含sendkey方法的使用
https://coderanch.com/t/635463/java/JNA-SendInput-function 包含sendkey方法的使用

- ApiViewer 2004 查看win32的常量的值 如 BM_CLICK （点击按钮）
- GetLastError() 值 列表： https://blog.csdn.net/theone10211024/article/details/14001943

