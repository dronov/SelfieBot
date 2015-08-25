<?php
  $xml="New content";
  if($xml)
   {
  $fh = fopen('info.xml', 'wr+');  
  fwrite($fh, $xml);  
   echo $xml;
   
   }else
   echo "XML is empty";
  //closing the file handler  
  fclose($fh); 
?>
