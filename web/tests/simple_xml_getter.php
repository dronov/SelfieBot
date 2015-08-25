<?php
  while(true){
    $xml = file_get_contents('info.xml');  
    if($xml){
      echo $xml;
    }
  }
?>
