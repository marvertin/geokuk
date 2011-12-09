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
Milí, uživatelé.
<p>
Od verze 3.3.0 máme novinku spočívající v možnosti
zobrazovat uživatelům zprávy při spuštění geokuku.
Zprávy si pomocí tlačítek vlevo a vpravo můžete prohlížet
a přečtení potvrdit tlačítkem "Přečteno".
Přečtené zprávy se už nebudou zobrazovat.

<h2>Vrácení zpět funkce "lovím" a oddělení od cest</h2>
Funkce "lovím", která označovala waypointy zeleně je nyní v původní podobě
a je nezávisla na vytváření cest. Obě funcionality můžete dle libosti využívat
na různé účely, například zeleně si označit keše, které vás zajímají z
dlouhodobého hlediska a cesty použít pro konkrétní výlet.
<?php } ?>

<? include "zastatistikuj.php"; ?>
</body>
</html>
