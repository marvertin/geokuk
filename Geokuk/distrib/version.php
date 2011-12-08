<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>  
<h1>[[<? include "version.txt"; ?>]]</h1>

<hr/>
<?php
  ini_set('display_errors',1);
  $msgnad = $_GET['msgnad'];  # Zobrazovat zprávy nad tím
  
  error_reporting(E_ALL|E_STRICT);
?>  
<?php if ($msgnad < 1) { ?>
<h1>==1==</h1>
Toto je zpráva číslo jedna.
<?php } ?>

<?php if ($msgnad < 2) { ?>
<h1>==2==</h1>
Toto je zpráva číslo 2.
A ta je delší <b>a s bólden</b>
<?php } ?>

<?php if ($msgnad < 3) { ?>
<h1>==3==</h1>
Toto je zpráva číslo 3.
Příliš žluťoučký kůň úpěl ďábelské ódy.
A ta je delší <i>a s kurzívou</i>
<?php } ?>
<? include "zastatistikuj.php"; ?>
</body>
</html>
