echo '开始运行'
mvn test -Dfile.encoding=UTF-8 -DskipUnitTest=false -DskipGetGitInfo=true -Dtrade.flags.dryRun=true -Dtest=cn/valuetodays/trade/doTrade#doTrade
