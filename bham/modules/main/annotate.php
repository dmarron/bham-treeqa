<?php
ini_set("memory_limit", "1000M");

$fdp=fopen($argv[1],"r");
$fdt=fopen($argv[2],"r");
$fdg=fopen($argv[3],"r");
$chr=$argv[4];
$id=$argv[5];
$pos=$argv[6];
$fdr=fopen($argv[1]."_conv","w+");
$map_name=array();
$line=0;
while(!feof($fdg)){
     $ret=fgets($fdg);
     $p=split(',',$ret);
     if (count($p)<2) break;
     $a=array();
     $a[0]=$p[$id];
     $a[2]=$p[$pos];
     $a[1]=$p[$chr];
     $map_name[$line]=$a;
     $line++;
}

while(!feof($fdp)){
     $r1=fgets($fdp);
     $r1=trim($r1);
     if (strlen($r1)==0) continue;
     $result=split("\t",$r1);
//     echo '='.$r1.'= ';
     $r2=fgets($fdt);
     $r2=trim($r2);
     $p=split("\t",$r2);
     
//     print_r($p);
     if ($map_name[(int) $p[0]][1]==$map_name[(int) $p[1]][1]) {
//	  echo "+".$r1."+\n";
	  fprintf($fdr,"%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$map_name[(int) $p[0]][1],$map_name[(int) $p[0]][0], $map_name[(int) $p[1]][0],$map_name[(int) $p[0]][2], $map_name[(int) $p[1]][2], $result[0], $result[1],$p[2]);
     }
}

//print_r($map_name);
?>