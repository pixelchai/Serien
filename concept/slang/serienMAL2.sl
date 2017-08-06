//v2.1
--init
    ^name =    "MyAnimeList"
    ^by =      "syano"
    ^type =    1
    ^base =    "https://myanimelist.net/"
    ^highres = 1
    ^rgx =     "(https?|ftp):\\/\\/[^\\s/$.?#].[^\\s]*"
----

--home--["hseasonal", "htop"]--

--seasonal_getstat
	eps= [$text(
	    $(%0, ".eps")[0]), null]
	score= [$trim(
	    $text($(%0, ".score")[0])), "stars"]
--[eps, score]--

--hseasonal
	elm = ".seasonal-anime"
	b=$load($concat(base, "anime/season"))

	links = @(*: $(b, elm+" .title-text a"))(*.$attr("href")) //implicit lambda
	titles = @(*: $(b, elm+" .title-text"))*.$text //postfix operator
	info = @(*:b.$elm)*.seasonal_getstat //var.method("arg2")

	imgelems = $(b, elm+" .image img")

	images = @*a:($trim(@(*x:
	                    @(*y:imgelems)$attr(y, "data-srcset")
	                 )(x~rgx))
	         +$trim(@(*x:
                   	    @(*y:imgelems)$attr(y, "srcset")
                   	 )(x~rgx)))a[0]

    images = @(x: $(b, elm+" .image img"))
                ?(x.$hasattr("data-srcset"))x.$attr("data-srcset")~rgx
                : x.$attr("srcset")~rgx
--
  [0,
  "Seasonal Anime",
  links,
  titles,
  images,
  info,
  null]
--