<?php
  function writeToXml($str){
    $fd = fopen("info.xml","a");
    fwrite($fd,$str);
    fclose($fd);
  }
  if(isset($_POST['selfiebotid'])){
  set_time_limit(0); 
  ob_implicit_flush(); 
    if (($socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) < 0) {
      echo "Error sock creation\n";
    }
    else {
      echo "Socket created\n";
    }
    $result = socket_connect($socket, "46.38.49.133", 4445);

 
    if ($result === false) {
      echo "Error while connecting";
    } else {
      echo "Connection successful\n";
    }

    $idStr = $_POST['selfiebotid']."\r";
    $msg = $idStr;
  
    echo "\nMsg to server: $idStr\n";
    socket_write($socket, $idStr, strlen($idStr)); 
    $out = socket_read($socket, 1024);
    echo "\nMsg from server: <<$out>>\n";
    
//     write logs
  writeToXml("\nMsg from server: <<$out>>\n");
    
    
  $xml='';
  while((strcmp($xml,"close")==0)==false){
    $xml = file_get_contents('cmd.xml');  
    if($xml){
	 socket_write($socket, $xml, strlen($xml)); 
	  writeToXml($xml);
      }
    }
    
    writeToXml ("Connection closed\n");
    if (isset($socket)) {
      socket_close($socket);
      writeToXml("Socket successfully closed\n");
    }
  echo "Listener closed";
  }
?>