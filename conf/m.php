<?php
@$act = $_GET['act'];
function dumpCmdInfo($ret){
	foreach($ret as $s){
		echo "$s"."<br/>";
	}
}
function getPid($ret){
	foreach($ret as $s){
		if(strpos($s, 'root')===0){
			$arr = explode('      ',$s);
			$arr = explode(' ',$arr[1]);
			return $arr[0];
		}
	}
	return "0";
}
$ret = array();
exec("netstat -an|grep 8586", $ret);
$portCnt = count($ret);
echo "端口监听情况：（下面没有条目时，才可以启动服务器！)";
echo "<br/>";
dumpCmdInfo($ret);
echo "<br/>";
if($act == 'upxml'){
	echo "更新策划数据：";
	unset($ret);
	$code = exec("sudo /usr/local/bin/svn up /usr/local/games/Design\ Doc/data", $ret);
	echo $code;
	echo "<br/>";
	dumpCmdInfo($ret);
	
	echo "发布策划数据到服务器：";
	unset($ret);
	$code = exec("ant -f /usr/local/games/qxmobile/build.xml deploy.conf", $ret);
	echo "<br/>";
	dumpCmdInfo($ret);
}

echo "<br/>";
unset($ret);
$code=exec('ps aux|grep java',$ret);
$pid =getPid($ret);
echo "Java进程：".$pid;
echo "<br/>";
dumpCmdInfo($ret);

/*
unset($ret);
exec("id",$ret);
echo "cmd id ret:";
dumpCmdInfo($ret);
*/
echo "<br/>";
echo "<a href='?act=upxml'>更新策划xml</a>";
echo "<br/>";
echo "<br/>";
$dateStr = exec('date "+%Y%m%d"');
$file = "/var/log/tomcat/catalina.out.".$dateStr;
if($act){
	if($act == 'stop'){
		echo "执行停服:<br/>";
		unset($ret);
		$code=exec("kill -9 ".$pid, $ret);
		dumpCmdInfo($ret);
	}else if($act == 'start'){
		echo "启动服务器:<br/>";
		unset($ret);
		$code=exec("/usr/local/games/server.sh start", $ret);
		dumpCmdInfo($ret);
	}
	echo "执行完毕。<a href='m.php'>刷新</a>";
}else if($portCnt>0){
	echo "服务器运行中。<a href='?act=stop'>停止</a>";
	echo "<br/>";
}else{
	echo "<br/>";
	echo "服务器未运行。<a href='?act=start'>启动</a>";
	echo "<br/>";
}
echo "<br/>";
echo "日志文件:";
echo $file;
echo "<br/>";
echo "<a href='?act=showLog'>显示日志</a>";
echo "<br/>";
if($act == 'showLog'){
	echo "<pre>";		
	system("tail -300 ".$file);
	echo "</pre>";		
}

echo "<br/>";
echo "<br/>";
echo "By 康";
?>
