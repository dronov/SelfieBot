<?
function writeToXml($str){
  $fd = fopen("cmd.xml","w");
  fwrite($fd,$str);
  fclose($fd);
}
echo "Joystick:";
//   header('Content-Type: text/plain;'); 
  set_time_limit(0); 
  ob_implicit_flush(); 
  if(isset($_POST['cmd'])){
  echo "post:";
      if(strcmp($_POST['cmd'],"left")==0){
        $cmd="aaaaa";
	echo "Turn left";
      }elseif(strcmp($_POST['cmd'],"right")==0){
      $cmd="ddddd";
      echo "Turn right";
      }elseif(strcmp($_POST['cmd'],"up")==0){
      $cmd="wwwww";
      echo "Turn up";
      }elseif(strcmp($_POST['cmd'],"down")==0){
      $cmd="sssss";
      echo "Turn down";
      }elseif(strcmp($_POST['cmd'],"stop")==0){
      echo "stop $cmd";
      }elseif(strcmp($_POST['cmd'],"close")==0){
      $cmd="qqqqq";
      }
      writeToXml($cmd);
      writeToXml(''); 
  }
?>