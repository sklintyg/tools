<?php

parseFile($_REQUEST['p']);

function parseFile($path) {
	$handle = fopen($path, "r");
	if (!$handle) {
		return;
	}
	while (($line = fgets($handle)) !== false) {
		while(preg_match("/.*PARM\{(.*)\}.*/", $line, $matches)) {
			if (strpos($matches[1], "name") !== false) {
				$line = str_replace("PARM{".$matches[1]."}", " [ ".$matches[1]." ] ", $line);			
			}
			else if (strpos($matches[1], "include") !== false) {
				if(preg_match("/include:\/(.*)/", $matches[1], $matches2)) {
//					echo $matches2[1];
					$includePath = $matches2[1];
					$includePath = parse_url($includePath);
					if ($includePath["path"] != "grp/grppoll-script.html" &&
						$includePath["path"] != "grp/grppoll_same-script.html" &&
						$includePath["path"] != "eid/_netid_scripts.html") {
						parseFile($includePath["path"]);
					}
					$line = str_replace("PARM{".$matches[1]."}", "", $line);							
				}
				else {
					$line = str_replace("PARM{".$matches[1]."}", " [ MISSING ".$matches[1]." ] ", $line);			
				}
			}
			else if (strpos($matches[1], "view") !== false) {
				$line = str_replace("PARM{".$matches[1]."}", "", $line);
			}
			else {
				$line = str_replace("PARM{".$matches[1]."}", " [ ".$matches[1]." ] ", $line);
			}
		}
		echo $line;
	}
	fclose($handle);
}

?>