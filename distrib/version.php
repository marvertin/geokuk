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

<?php if ($msgnad < 2) { ?>
<h1>==2==</h1>
<h2>Nová služba pro vyhledávání adres a jiných názvů zeměpisných míst (Ctrl+M).</h2>
Od verze 3.5 je použita nová služba Google. Popis služby je zde:
<a href="https://developers.google.com/maps/documentation/geocoding">https://developers.google.com/maps/documentation/geocoding</a>.
Základním rozdílem oproti dříve používané službě je limit v počtu provedených dotazů na 2500 / 24 hodin. Zdá se to jako dost,
ale vzhledem k tomu, že vyhledávání probíhá při každém stisku znaku v okně Ctrl+M, může být limit vyčerpán.
Pokud se to ukáže jako problé, budu počty hledání optimalizovat.
<?php } ?>

<? include "zastatistikuj.php"; ?>
</body>
</html>
