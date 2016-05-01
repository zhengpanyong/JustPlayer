#Read me
This is an Android music player, which is very light weight.It only contains most used feature：play local music，find lyric from Internet.However，it looks beautiful. This is my first application，so there must be some bugs in it. This just for practice.
## Note:
**I share this project just for someone who want to study Android. Nobody should use it for other usage, and please do not change it or publish on other sites.**

##Summary
###Activity
This project contains three activities: MainActivity, PlayActivity and SearchActivity. 
1. MainActivity: contains a music list and a slide menu.
2. PlayActivity: has two fragments,one shows the album picture while another shows lyric.
3. SearchActivity: you can search local musics in this activity

###Service
Two main services in this project
1. PlayService: the main service of this project, it is responsible to playing music. The MediaPlayer is here, PlayService controls the status of MediaPlayer.
2. StatusService: this is an class which is not Android service.It controls the whole status of this app, such as repeat mode, which music is playing, providing music list and album picture to activitys. It is a singleton class. when it is created, it load properties from a SharedPreference file, and when destroyed, it save properties to SharedPreference file.

###Util
1. BitmapBlur: use RenderScript to blur bitmap which can be used as the background of PlayActivity.
2. FileUtil: load musics from content provider and returns an ArrayList of musics. it also uses thread to find lyric from Internet according to the song name and artist, if succeed, it save lyric as file in floder LightPlayer. When complete, call the method of onComplete(int,File) in OnLrcCompleteListener to set lyric to LrcUtil. 
3. LrcUtil: this class uses Regexp to analyze lyric file and returns a TreeMap which key is time and value is lyric sentence.

###View
1. SlideLayout: it is a slide layout which is an custom viewgroup.do not depend any third part lib. and easy to use.
2. LrcView: it is an custom view which display lyric. you can slide it to view lyric, and it can smooth sroll depend on the time you provide through setTime() method. you can also set the text size,text color and so on if you will.





