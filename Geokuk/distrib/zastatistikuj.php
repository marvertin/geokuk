<?php
  $verze = $_GET['verze'];
  $ipadresa = $_SERVER['REMOTE_ADDR'];
  $link = mysql_connect("localhost", "geokuk_cz03", "geokukzphp")
      or die("Nelze se pripojit: $php_errormsg");
   mysql_select_db("geokuk_cz_stat", $link)
      or die("Nelze vybrat  : $php_errormsg");
   $result = mysql_query("INSERT INTO geokukstat (ipadresa, verze) VALUES ('$ipadresa', '$verze')");
   if (!$result) {
     die('Could not query:' . mysql_error());
   }
?>
