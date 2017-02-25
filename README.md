Hyde (v2)
=========

Hyde is an application that lets you "hide" your desktop.
This can be convenient when you want to work in one application for a while --
such as a text editor or IDE -- and that application isn't full-screen
and you don't want to see your messy desktop behind it.
That's all this application does.


Compiling
---------

Feb., 2017: I could not get SBT to compile the project properly until I copied 
*rt.jar* into the *lib* folder. I didn't investigate why, but the compilation 
kept failing on *com.apple.eawt* errors until I did that.


Status
------

I just started converting the very old Java code to this new Hyde2 project.
What I want to do in Hyde2 is strip out all of the old licensing code and
sound effects, while also adding the ability to show multiple Hyde windows.
The original Hyde application could only display one window.

Eventually I also want to use this as an experimental application that I 
can apply ProGuard to, to see how small I can make the resulting jar file
and application (by removing all the class files I don't need).


Demo
----

The best way to learn about Hyde is to see a few pictures and
watch a short video. You can find all those goodies here:

  http://alvinalexander.com/hide-desktop-and-desktop-icons


More Information
----------------

Hyde was created by Alvin Alexander at http://alvinalexander.com



