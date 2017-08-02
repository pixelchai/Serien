-init
@name "MyAnimeList"
@by "syanochara"
@type 1
@base "https://myanimelist.net/"
@highres 1
@rgx "(https?|ftp):\\/\\/[^\\s/$.?#].[^\\s]*"
-

-home-["hseasonal" "htop"]

-seasonal_getstat
	#eps [($text 
			([ ($ %0 ".eps") 0)
			) null]
	#score [($trim ($text 
			([ ($ %0 ".score") 0)
			)) "stars"]
-[+eps +score]

-hseasonal
	#elm ".seasonal-anime"
	#b ($load($concat +base "anime/season"))

	#links *($attr ($ +b($concat +elm " .title-text a")) "href")
	#titles *($text ($ +b($concat +elm " .title-text")))

	#imgelems ($ +b($concat +elm " .image img"))
	#images *([($concat 
				($trim *(~ *($attr +imgelems "data-srcset") +rgx))
				($trim *(~ *($attr +imgelems "srcset") +rgx))
			)+highres)
	#info *(seasonal_getstat($ +b +elm))
-[0 "Seasonal Anime" +links +titles +images +info null]

-top_getstat
	#eps [($trim ([ (~ ($html 
			([ ($ %0 ".information") 0)
			) "[^<>]+") 0)) null]
	#score [($text 
			([ ($ %0 ".score") 0)
			) "stars"]
-[+eps +score]

-htop
	#b ($load($concat +base ($concat "/topanime.php?limit=" ($imult %0 50)))) ;TODO pagination

	#links *($attr ($ +b ".ranking-list .title .clearfix .hoverinfo_trigger") "href")
	#titles *($text ($ +b ".ranking-list .title .clearfix"))

	#imgelems ($ +b ".ranking-list img")
	#images *([
				($trim *(~ *($attr +imgelems "data-srcset") +rgx))
			+highres)
	#info *(top_getstat($ +b ".ranking-list"))
-[3 "Top Anime" +links +titles +images +info "htop"]