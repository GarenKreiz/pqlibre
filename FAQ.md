# General #

> **Q**: Why are you doing this?
> > **A**: Groundspeak did not invent geocaching. The sport was invented by Dave Ulmer, who placed the first cache, and Mike Teague, who created the first online database of caches, and Matt Stum, who coined the word "geocaching". Mike Teague's database was given, free of charge, to Groundspeak. Groundspeak does not create caches. Groundspeak does not review caches. All the work of creating, maintaining, and reviewing caches is done by unpaid volunteers.  Meanwhile, they have a near-monopoly on cache listings and trackable sales, in addition to branded merchandise and subscriptions. Those that want to cache on a smartphone must buy the official app or use apps that pay Groundpseak to use the official API. The restrictive license on the API [excludes open-source developers.](http://faq.cgeo.org/#1_21) In short, Groundspeak has greedily taken over geocaching and are putting their own profits ahead of the good of the sport.


> **Q**: Does it cost me anything?
> > **A**: No. PQ Libre is free, and always will be.

# Technical #


> **Q**: Does this use a lot of bandwidth?
> > **A**: PQ Libre has been optimised to use as little bandwidth as possible.


> **Q**: How can I speed up my searches?
> > **A**: Filter on type, number of favourite points, placed date, keyword, contains trackable, and days since last find or not found at all. Ignore own, found, and disabled. Also filter on "Needs Maintenance" attribute. The more of these search criteria you have, the more caches can be rejected before downloading the full description. Filtering on size, difficulty, and terrain also helps to a lesser degree. Reducing the search radius and/or limiting the number of caches returned will also help return your result faster. Deselecting the "include full logs" checkbox will speed up the retrieval of individual caches that match your criteria. Check the "ignore own" box even if you have not placed any caches.


> **Q**: I have a great idea for a feature you should include!
> > **A**: Great! Drop us an email or create a feature request using the "Issues" tab. Describe your idea in as much detail as possible. Feel free to send us an image if you think that helps.


> **Q**: No caches or only some caches are included in the search result.
> > **A**: This may be a date format related issue. Try the following steps:
      1. If you haven't already done so, upgrade to Java 7.
      1. Go to http://www.geocaching.com/account/ManagePreferences.aspx
      1. Change your date format to a format with only numbers, e.g. instead of 29/Dec/2012, use 29/12/2012.
> > > If none of this helps, please file a new bug report on the ["Issues" page](http://code.google.com/p/pqlibre/issues/list).



> **Q**: I'm using 64-bit Windows but the 64-bit version does not work.
> > **A**: Windows is a bit confusing when it comes to what version you should choose. The determining factor is what version of Java you have, not what version of Windows you have: Sometimes people have 64-bit Java on 32-bit Windows, sometimes people have 32-bit Java on 64-bit windows. If it does not work with the 64-bit version, try with the 32-bit version instead, and vice versa.



> **Q**: I'm using 32-bit Windows but the 32-bit version does not work.
> > **A**: See above.


> **Q**: Why does the progress bar pause when saving a GPX file?
> > **A**: A valid GPX file requires the user ID of the cache owner. This is not automatically included in the cache info, so PQLibre needs to do a lookup on the cache owner's name. This used to be done during the search phase, but is now done during the file save phase so as to speed up the search - after all the owner's ID is not needed if you choose the map output or to open the caches in a browser. PQLibre saves the IDs of all users it encounters, so the more you use it the less likely a pause becomes.


> **Q**: When pressing the "Show Results in Browser" button I only see one cache, despite the search finding several.
> > **A**: This is a known problem with Internet Explorer. The suggested resolution is to install [Firefox](http://www.mozilla.org/en-US/firefox/) or [Chrome](http://www.google.com/chrome) and using that as your default browser.



> **Q**: I can't log in.
> > **A**: Are you using the latest PQLibre release? If not, please upgrade. If that does not help, try switch your geocaching.com language setting to English by logging in using your browser and going to this page: http://www.geocaching.com/account/ManagePreferences.aspx and send us a bug report describing the problem in as much detail as possible.


> **Q**: It's not working!
> > **A**: Sorry to hear that. Please email us or, even better, create a bug report using the "Issues" tab. When you do, please describe the problem in as great detail as possible. Include information about what OS you are using, what version of the OS, and what version of Java you are using. Describe the exact steps you followed to produce the problem, and exactly what happens when you encounter the problem. If possible, please include the `.gpxexporter.properties` file you can find in your home directory. Also, try [running PQLibre from the command line](RunningCommandLine.md) and tell us what the result is. The more details you include, the faster we can fix the problem. Thanks!



> **Q**: How do I make it as easy as possible for you to fix the bug I discovered?
> > **A**: By emailing us the `.gpxexporter.properties` file you can find in your home directory. Also, try [running PQLibre from the command line](RunningCommandLine.md) and tell us what the result is.