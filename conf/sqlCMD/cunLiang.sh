#crontab -e 配置
#1 0 * * * /data/wushuang/test-server/cunLiang.sh >>/data/qxcron.log & 2>&1
#游戏库配置
gameDBHost="10.66.143.160";
gameDBPort="3306";
gameDBUser='111';
gameDBPwd='111';
gameDBName='ws01';
dateStr=`date -d last-day +%Y%m%d_%H%M%S`;
newTableName="cunliang_$dateStr";
#-----------------------
#日志库配置
logDBHost="112.90.5.111";
logDBPort="90";
logDBUser='qxws';
logDBPwd='5xAsus9bWxdX6AaA';
logDBName='sourcedata_tlog_qxws';

#复制表
cmd="mysql -h$gameDBHost -P$gameDBPort -u$gameDBUser -p'$gameDBPwd' -D$gameDBName -e 'create table $newTableName (select * from CunLiang)'";
echo "$cmd" | sh;
echo "执行复制表 结果 $?";
if [ $? = 0 ]; then
	echo "复制成功，刷新数据";
	cmd="mysql -h$gameDBHost -P$gameDBPort -u$gameDBUser -p'$gameDBPwd' -D$gameDBName -e 'update CunLiang set islogin=0,todayonlinetime=0'";
	echo "$cmd" | sh;
	echo "刷新数据 结果 $?";
fi

#导出表
cmd="mysqldump --skip-opt --no-create-info  -h$gameDBHost -P$gameDBPort -u$gameDBUser -p'$gameDBPwd' $gameDBName $newTableName>$newTableName.sql";
echo "$cmd"|sh; 
echo "执行dump 结果 $?";

#导入到日志库
#createTable
sql="CREATE TABLE $newTableName ( \
  RoleId bigint(20) NOT NULL, \
  CareerId int(11) NOT NULL, \
  Fight int(11) NOT NULL, \
  LoginChannel int(11) NOT NULL,\
  PlatId int(11) NOT NULL, \
  RoleName varchar(255) DEFAULT NULL, \
  VipPoint int(11) NOT NULL, \
  diamondandroid int(11) NOT NULL, \
  diamondios int(11) NOT NULL, \
  gameappid varchar(255) DEFAULT NULL, \
  iFriends int(11) NOT NULL, \
  islogin int(11) NOT NULL, \
  ispay int(11) NOT NULL, \
  level int(11) NOT NULL, \
  moneyandroid int(11) NOT NULL, \
  moneyios int(11) NOT NULL, \
  openid varchar(255) DEFAULT NULL, \
  regtime datetime DEFAULT NULL, \
  todayonlinetime int(11) NOT NULL,  \
  todaypay int(11) NOT NULL, \
  totaltime int(11) NOT NULL, \
  zoneid int(11) NOT NULL \
) ENGINE=MyISAM DEFAULT CHARSET=utf8;";
cmd="mysql -h$logDBHost -P$logDBPort -u$logDBUser -p$logDBPwd -D$logDBName -e '$sql'";
echo "$cmd" | sh;
echo "创建表 结果 $?";

cmd="mysql -h$logDBHost -P$logDBPort -u$logDBUser -p$logDBPwd -D$logDBName --default-character-set=utf8<$newTableName.sql";
echo "$cmd" ;
echo "$cmd" |sh;
echo "导入到日志库结果 $?";

