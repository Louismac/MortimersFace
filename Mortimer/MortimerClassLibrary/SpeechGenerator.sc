SpeechGenerator {

	var <>interuptions;
	var comment,ctr=0;

	*new {
		^super.new.initSpeechGenerator;
	}

	initSpeechGenerator {

		"initSpeechGenerator".postln;

		Speech.init(2);
		Speech.channels[0].voice_(3);
		interuptions=List.new(0);
	}

	fnGetInterrupt {
		var parts=[
			"Moving on, ",
			"In a hurry? ",
			"Woaaah there, ",
			"O k o k, "
			"Moving swiftly on, ",
			"No need to interruppt, "
			"Let me finish, "
		];

		^parts.choose;
	}

	fnGetIntro {arg remindAboutFB=false;
		var intro="";

		remindAboutFB=false;

		intro=this.fnGetGreeting++", let me tell you a little bit about myself. Years of rock
		and roll drumming and living the high life have left me completely deaf.
		Luckily, I can understand you're piano playing through the magic of MIDI.
		You can talk to me using the phone infront of you.";

		if(remindAboutFB,{
			intro=intro++" Also, I have sent you a friend request on Facebook, if you accept it I can post pictures of our sessions! "
		});

		intro=intro++" Anyway, ";

		intro=this.fnGetGreeting++", ";

		^intro.postln;
	}

	fnCompareStat{arg stat,data;
		var sorted,j;

		sorted=List.new(0);

		data.size.do{arg i;
			sorted.add([data[i][stat],i+1]);
		};

		for(1,sorted.size-1,{arg i;
			j=i;
			while({(j>0)&&(sorted[(j-1).max(0)][0]>sorted[j][0])},{
				sorted.swap(j,j-1);
				j=j-1;
			});
		});

		^sorted;

	}

	fnExtendComment{arg extension;
		var ptr=0,prevEnd;
		if(ctr<4,{
			prevEnd=" ";
			while({prevEnd==" "},{
				prevEnd=comment.last.asString;
			});
			["prevEnd",prevEnd].postln;
			if((prevEnd==".")||(prevEnd=="!")||(prevEnd=="?"),{
				while({extension[ptr].asString==" "},{
					ptr=ptr+1;
				});
				if(ptr>0, {
					extension=extension[0..(ptr-1).max(0)]++extension[ptr].asString.capitalize++extension[ptr+1..extension.size-1];
				});
			});
			if(ctr>0,{
				comment=comment++". ";
			});
			comment=comment++extension;
			["extension",comment].postln;
			ctr=ctr+1;
		});
	}

	fnCorrectGrammar {arg str;

		var newStr,skip,capitalise;

		newStr="";

		skip=false;
		capitalise=false;

		str.do{arg s,i;
			s=s.asString;
			skip=false;
			if(s==" ",{
				if(s[i-1]==" ",{skip=true});
			},{
				if((s==".")||(s=="!")||(s=="?"),{
					capitalise=true;
				},{
					if(capitalise,{
						s=s.capitalize;
						capitalise=false;
					});
				});
			});

			if(skip==false,{
				newStr=newStr++s;
			});
		}

		^newStr;
	}

	fnGetPost{
		var phrases;

		phrases=[
			" my post",
			" the thing that I posted",
			" what I posted",
		];

		^phrases.choose;
	}

	fnGetThanksFor{
		var phrases;

		phrases=[
			" Thanks for",
			" I'm happy that you",
			" Thank you for",
			" I liked that you",
			" I'm glad that you",
			" Cheers for"
		]

		^phrases.choose;
	}

	fnGetReply {
		var phrases;

		phrases=[
			" the comment",
			" commenting",
			" getting in touch",
		];

		^phrases.choose;
	}

	fnGetDayComment {arg day;
		var phrases;

		phrases=[
			" "+day++"tastic",
			" Rocking on a "++day,
			" Fun on a "++day,
			" My favourite thing to do on a "++day,
			" Not your usual "++day,
			" "++day++" Funday"
		];

		^phrases.choose

	}

	fnGetReally{
		var phrases;

		phrases=[
			" really",
			" amazingly",
			" super",
			" crazy",
			" crazily",
			" mega",
			" reet",
		];

		^phrases.choose;
	}

	fnGetWeekComment{arg week;
		var phrases,ranked;

		ranked = case
		{week==1} {"1st"}
		{week==2} {"2nd"}
		{week==3} {"3rd"}
		{week==4} {"4th"}
		{week==5} {"5th"}
		{week==6} {"6th"};


		phrases=[
			" Its week number "++week,
			" Its our "++ranked++" week",
			" This was our "++ranked++" week",
			" We've played "++week++" times together now",
		];

		^phrases.choose;
	}

	fnGetLastWeek {
		var phrases;

		phrases=[
			" last week",
			" the other day",
			" during the week",
		];

		^phrases.choose;
	}

	fnGetCommentOnSession{arg data;
		var rankedStats,stats;
		var currentWeek;
		var highest,lowest,higher,lower;
		var possibleComments;

		possibleComments=List.new(0);

		stats=[\meanComplexity,\meanTempo,\maxTempo,\maxComplexity,\sessionLength,\timePlaying];
		currentWeek=data[\weekData].size;

		ctr=0;

		comment=this.fnGetGreeting;
		comment=comment++" "++data[\name].split($_)[0];
		comment=comment++".";
		this.fnExtendComment(this.fnGetPraise);

		if(data[\fbData]!=nil, {
			if(data[\fbData][0][1]=="Like",{
				this.fnExtendComment(this.fnGetThanksFor++ " Liking"++this.fnGetPost++"!");
			});

			if(data[\fbData][0][1]=="Comment",{
				this.fnExtendComment(this.fnGetThanksFor++ this.fnGetReply++"!");
			});

			if(data[\fbData][1][1]=="true",{
				this.fnExtendComment(this.fnGetThanksFor++ " the message! Facebook won't let me read it, but I'll asume it was nice");
			});
		});

		data[\photoData].postln;
		data[\fbData].postln;

		if(0.5.coin, {
			this.fnExtendComment(this.fnGetWeekComment(currentWeek));
		},{
			this.fnExtendComment(this.fnGetDayComment(data[\dayOfWeek]));
		});

		if((0.5.coin)&&(data[\photoData].size>0),{
			//Use photo data

			"using photo".postln;
			if(data[\photoData][\currentSection]=="breakdown",{
				possibleComments.add(" Here"++this.fnGetWe++"are breaking it down");
			});

			if(data[\photoData][\tempo]>0.7,{
				possibleComments.add(" Here"++this.fnGetWe++"are going"++this.fnGetReally++"  fast");
			});

			if(data[\photoData][\tempo]>0.25,{
				possibleComments.add(" Here "++this.fnGetWe++"are going "++this.fnGetReally++"  slow");
			});
		});

		//Were this week's stats particarly high or low?

		data[\weekData].postln;
		data[\weekData].last.postln;
		if(data[\weekData].size>0, {
			if(data[\weekData].last[\maxTempo]>0.9,{
				possibleComments.add(" This week"++this.fnGetWe++"got up to some crazy speeds");
			});

			if(data[\weekData].last[\meanComplexity]>0.75,{
				possibleComments.add(this.fnGetWe++"were keeping it "++this.fnGetReally++"  complex throughout");
			});

			//How were this week's stats in comparision to other weeks?

			currentWeek.postln;
			if(currentWeek>1, {
				rankedStats=();
				highest=List.new(0);
				lowest=List.new(0);
				lower=List.new(0);
				higher=List.new(0);

				stats.do{arg key;
					//Sort stats
					rankedStats[key]=this.fnCompareStat(key,data[\weekData]);

					//[currentWeek,rankedStats[key]].postln;

					//See how they compare
					if(rankedStats[key].last[1]==currentWeek,{
						highest.add(key);
					});
					if(rankedStats[key][0][1]==currentWeek,{
						lowest.add(key);
					});
					if(currentWeek>1, {
						if(data[\weekData].last[key]>data[\weekData][currentWeek-2][key],{
							higher.add(key);
						});
						if(data[\weekData].last[key]<data[\weekData][currentWeek-2][key],{
							lower.add(key);
						});
					});
				};

				highest.do{arg key;
					case
					{key==\maxTempo} {
						possibleComments.add(this.fnGetWe++"played the fastest we've ever played"++this.fnGetThisTime);
					}
					{key==\maxComplexity} {
						possibleComments.add(this.fnGetWe++"played the most complicated we've ever played"++this.fnGetThisTime);
					}
					{key==\sessionLength} {
						possibleComments.add(this.fnGetWe++"played for the longest we've ever played for"++this.fnGetThisTime);
					}
					{key==\timePlaying} {

					};
				};

				lowest.do{arg key;
					case
					{key==\maxTempo} {
						possibleComments.add(this.fnGetWe++" played the slowest we've ever played"++this.fnGetThisTime);
					}
					{key==\maxComplexity} {
						possibleComments.add(this.fnGetWe++" played the simplest we've ever played"++this.fnGetThisTime);
					}
					{key==\sessionLength} {
						possibleComments.add(this.fnGetWe++" played the shortest time we've ever played for"++this.fnGetThisTime++". Are you getting bored?");
					}
					{key==\timePlaying} {

					};
				};

				lower.do{arg key;
					case
					{key==\meanTempo} {
						possibleComments.add(this.fnGetWe++" played slower"++this.fnGetThisTime++" than we played"++ this.fnGetLastTime);
					}
					{key==\meanComplexity} {
						possibleComments.add(this.fnGetWe++" played simpler"++this.fnGetThisTime++" than we played"++ this.fnGetLastTime);
					}
					{key==\sessionLength} {
						possibleComments.add(this.fnGetWe++" played for less time"++this.fnGetThisTime++" than we played for"++ this.fnGetLastTime);
					}
					{key==\timePlaying} {

					};
				};

				higher.do{arg key;
					case
					{key==\meanTempo} {
						possibleComments.add(this.fnGetWe++" played faster"++this.fnGetThisTime++" than we played "++this.fnGetLastTime);
					}
					{key==\meanComplexity} {
						possibleComments.add(this.fnGetWe++" played more complicated"++this.fnGetThisTime++" than we played "++this.fnGetLastTime);
					}
					{key==\sessionLength} {
						possibleComments.add(this.fnGetWe++" played for longer"++this.fnGetThisTime++" than we played for "++this.fnGetLastTime);
					}
					{key==\timePlaying} {

					};
				};
			});
		});

		//Pick a comment and add
		if(possibleComments.size>0,{this.fnExtendComment(possibleComments[possibleComments.size.rand])});

		this.fnExtendComment(this.fnGetStart);

		comment=this.fnCorrectGrammar(comment);

		^comment;
	}

	fnGetThisTime {
		var phrases=[
			" today",
			" in this session",
			" this week",
			" this time"
		];
		^phrases.choose;
	}

	fnGetLastTime {
		var phrases=[
			" last time",
			" in the previous session",
			" last week",
		];
		^phrases.choose;
	}

	fnGetGreeting {
		var phrases=[
			"Hello",
			"Hi",
			"Salutations",
			"Why, Hello there",
			"Greetings",
			"Hey up",
			"Hi-ya",
			"Well look who it is"
		];
		^phrases.choose;
	}

	fnGetFriend {
		^[
			", old buddy old pal?",
			", good friend of mine?",
			", maestro?",
			", rock and roll star?",
			", mate?",
			", friend?",
			", pal?",
			", croney?",
			", sidekick?",
			", admiral?",
			", guvnor?"
		].choose
	}

	//Generate a greeting, should expect a yes or no answer
	 fnGetInvite {
		var greeting, parts;

		parts=();

		//Question beginning
		parts[\a]=[", Would you like to",
			", How about we",
			", Can we please",
			", Would it suit you to",
			", You and me could",
		];

		//Question ending
	    parts[\b] = [
			" have a jam",
			" play some sweet music",
			" make some groovy vibes",
			" get down to some tunage",
			" have a hoe down",
		];

		greeting = parts[\a].choose++parts[\b].choose++this.fnGetFriend;

		^greeting.postln;
	}

	fnGetNevermind {
		^[
			" nevermind",
			" no problem",
			" thats o k",
			" no worries",
			" no biggy",
			" don't sweat it",
			" it doesn't matter"
		].choose;
	}

	fnGetReassurance {
		var phrases;
		phrases=[
			" Everybody makes mistakes. ",
			" These things happen",
			" I've got all day",
			" I'm not going anywhere",
		];
		^phrases.choose
	}

	fnGetStopped {arg data;
		var phrase,falseStart;
		//Depending on how long you were playing and why you stopped, alter speech
		phrase=this.fnGetNevermind;
		case
		{data[\stopped]=="button"} {
			phrase=phrase++this.fnGetReassurance;
		}
		{data[\stopped]=="silence"} {
			phrase=phrase++" Sorry I got abit carried away there. ";
		};

		if(data[\barsSinceStop]>12,{
			phrase=phrase++this.fnGetWe++" were on"++this.fnGetAbit++" of a run there";
		},{
				falseStart=[
					" had a false start",
					" didn't really get going",
					" messed up early",
				].choose;
				phrase=phrase++this.fnGetWe++falseStart;
		});

		^phrase
	}

	//At end of song. Requires yes or no answer
	fnGetEnd {
		var phrase;
		phrase=this.fnGetPraise;
		phrase=phrase++this.fnGetRequest;
		phrase=phrase++this.fnGetPlay;
		phrase=phrase++" again?"
		^phrase;
	}

	fnGetPraise {
		^[
			" Great Jam",
			" I really enjoyed that",
			" Excellent",
			" What Fun!",
			" That was superb",
			" That was amazing",
			" How about that?",
			" Well done",
			" Good effort",
			" Mad skills",
			" I think were getting better",
			" Good going",
			" Niiiiiice"
		].choose;
	}

	fnGetAbit {
		^[
			" a bit",
			" a little bit",
			" a wee bit"
		].choose;
	}

	fnGetNextTime {
		^[
			" next time",
			" when "++this.fnGetWe++" go again",
			" in future",
			" the next time round",
		].choose;
	}

	fnGetPlay {
		^[
			" play",
			" jam",
			" go",
			" perform",
			" do it"
		].choose;
	}

	fnGetMaybe {
		^[
			"Maybe ",
			"Perhaps ",
			"Possibly ",
			"Is there a chance "
		].choose;
	}


	fnGetRequest {
		^[
			" Would you like to",
			" Do you want to"
			" How about "++this.fnGetWe,
			" Can"++this.fnGetWe++" please",
			" Would it suit you to",
			" You and me could",
		].choose;
	}

	fnGetGreat {
		^[
			" great ",
			" awesome ",
			" wicked ",
			" brillo ",
			" fantastic ",
			" Good going ",
			" Niiiiiice ",
			" Excellent"
		].choose;
	}

	fnGetWe {
		^[
			" we ",
			" me and you ",
			" the two of us ",
			" you and I ",
			" both of us "
		].choose;
	}

	//Asking if you want to change settings. Requires Yes or No answer
	fnGetChange {
		var phrase,change;
		change=[
			" change it up?",
			" mix it up?",
			" change it around?",
			this.fnGetPlay++" differently"++this.fnGetNextTime++"?",
			this.fnGetPlay++this.fnGetAbit++" differently?"
		];
		phrase=this.fnGetRequest++change.choose;
		^phrase

	}

	//On opening of settings page
	fnGetHow {arg first=false;
		var phrase;
		phrase="How Shall "++this.fnGetWe++this.fnGetPlay;
		if(first==false,{phrase=phrase++this.fnGetNextTime});
		^phrase
	}

	fnGetAgree {
		var phrases;
		phrases=[
			"O K ",
			"Alright ",
			"Good stuff "
			"Right "
			"I hear you "
		];
		^phrases.choose;
	}

	//Before count in. Requires Yes or No answer
	fnGetLeadIn {arg stopCtr=0;
		var phrase;
		phrase=this.fnGetAgree ++" I'll count you in ";
		if(stopCtr==0,{phrase=phrase++" one bar"});
		phrase=phrase++". Are you ready?";
		^phrase
	}

	//On termination (I dont want to play, Im sure)
	fnGetGoodbye {arg takenPhoto=false;
		var phrase;

		phrase=this.fnGetAgree ++" see you later";

		if(takenPhoto,{
			phrase=phrase++". I'll post some pictures later"
		});

		^phrase;
	}

	//On first attempt to leave
	fnGetAreYouSure {
		^"Are you sure? I won't come back"
	}

	//Following a "no" answer to "Are you ready?"
	fnGetWait {
		^this.fnGetNevermind++" I can wait, let me know when you want to"++this.fnGetPlay;
	}

	//Following yes answer to "Are you ready?"
	fnGetStart {
		^[
			" Lets go!",
			" Rock and roll!",
			" Kick it!",
			" Jam on it!",
			" Boogie down!",
			" Whammmy!"
		].choose;
	}

	fnGetComeBack {
		^[
			" Nice to have you back, ",
			" I thought you were going to leave me there but, "
			" Phew that was close ",
			" Thanks for giving me another chance "
		].choose;
	}
}