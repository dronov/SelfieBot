<?
    if(isset($_POST['selfiebotid'])){
      $address = '46.38.49.133'; 
      $port = 4445;

      if (($socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) < 0) {
	echo ($_SERVER['REMOTE_ADDR'])?"Error sock creation\n":"Not connected";
      }
      else {
	echo ($_SERVER['REMOTE_ADDR'])?"Socket created\n":"Not connected";
      }
      $result = socket_connect($socket, $address, $port);

    
      if ($result === false) {
	echo ($_SERVER['REMOTE_ADDR'])?"Error while connecting\n":"Not connected";
      } else {
	echo ($_SERVER['REMOTE_ADDR'])?"Successful connection to proxy\n":"Not connected";
      }

      $idStr = "G987654321";
      
      socket_write($socket, $idStr, strlen($idStr)); 
      echo ($_SERVER['REMOTE_ADDR'])?"output\n":"Not connected";
      $out = socket_read($socket, 32);
//       if(strcasecmp($out,"\r\nCONNECT\r\n")==0){
// 	echo ($_SERVER['REMOTE_ADDR'])?"Connected to ".$_POST['selfiebotid']:"Not connected";}
      echo "Connection closed\n";
      if (isset($socket)) {
	socket_close($socket);
	echo "Socket successfully closed";
   
   }
  }
  if(isset($_POST['left'])){
    echo ($_SERVER['REMOTE_ADDR'])?"Turn left":"Not connected";
    }
   if(isset($_POST['right'])){
    echo ($_SERVER['REMOTE_ADDR'])?"Turn right":"Not connected";
    }
       if(isset($_POST['up'])){
    echo ($_SERVER['REMOTE_ADDR'])?"Turn up":"Not connected";
    }
    if(isset($_POST['down'])){
    echo ($_SERVER['REMOTE_ADDR'])?"Turn down":"Not connected";
    }
    if(isset($_POST['stop'])){
    echo ($_SERVER['REMOTE_ADDR'])?"Stop":"Not connected";
    }
?>