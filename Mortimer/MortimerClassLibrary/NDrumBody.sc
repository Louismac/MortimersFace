NDrumBody {

	var <>speech,wordCtr,isTalking,shouldTalk,numWords;
	var <>face;
	var <>head;
	var tempo;

	*new {arg config;
		^super.new.initNDrumFace(config);
	}

	initNDrumFace {arg config;

		"initBody".postln;

		speech=SpeechGenerator.new;
		shouldTalk=config[\shouldTalk];
		face=NDrumFace.new(config[\shouldAnimateFace],config[\processing]);
		if(config[\robot]==false, {config[\shouldMoveHead]=false});
		head=NDrumHead.new(nil,config[\board],config[\shouldMoveHead]);

		wordCtr=0;
		numWords=0;

		Speech.wordAction_({this.fnWordEnded});
		Speech.doneAction_({face.fnCloseMouth;isTalking=false});
	}

	fnReset {
		Speech.channels[0].stop;
		face.fnStopBlinking;
		head.fnCenterHead;
	}

	fnGetLog {
		var bodyLog;

		bodyLog=["HEAD"]++head.fnGetLog++["FACE"]++face.fnGetLog;

		^bodyLog;
	}

	fnResetLog {
		head.fnResetLog;
		face.fnResetLog;
	}

	fnSetTempo {arg t;
		tempo=t;
		head.fnSetTempo(t);
	}

	//SPEECH

	fnSpeak {arg str;

		//MAKE SOUND

		var cutOff;

		isTalking=true;

		if(shouldTalk==false,{^"Action not completed"});

		cutOff=4;

		//If already talking
		if(Speech.channels[0].isActive,{
			"Already talking".postln;
			speech.interuptions.add(Date.getDate.stamp);
			//If few words left, finish and queue new utterance
			if((numWords-wordCtr)<cutOff,{
				"Queuing".postln;
				Speech.doneAction_({
					{1.do{
						(0.01).wait;
						wordCtr=0;
						numWords=str.split($ ).size;
						this.fnSpeak(str);
					}}.fork;
					Speech.doneAction_({face.fnCloseMouth(true);isTalking=false});
				});
			},{
			//If many words left, cut off
				"Interrupt".postln;
				Speech.channels[0].stop;
				str=speech.fnGetInterrupt;
				wordCtr=0;
				//Speech.channels[0].speak(str);
				numWords=str.split($ ).size;
				{(0.5).wait;this.fnSpeak(str);}.fork;
			});
		},{
			wordCtr=0;
			numWords=str.split($ ).size;
			Speech.channels[0].speak(str);
		});

		//MOVE FACE

		face.fnOpenMouth(true);
	}

	fnWordEnded {
		wordCtr=wordCtr+1;
		{
			face.fnCloseMouth(true);
			0.15.wait;
			if(isTalking,{face.fnOpenMouth(true)});
		}.fork;
	}

	fnTense {
		face.fnTension;
	}

	fnSmile {
		face.fnSmile;
	}

	//SOCIAL

	fnEndSong {

		{1.do{
			head.fnCenterHead;
			face.fnSmile;
			1.wait;
			this.fnSpeak(speech.fnGetEnd);
			2.wait;
			face.fnInquisitive;
			face.fnSmile;
			head.fnLeanForwards;
		}}.fork;
	}

	fnStartSong {
		head.fnCenterHead;
		this.fnSpeak(speech.fnGetStart);
		face.fnSmile;
	}

	fnLeadIn {arg stopCtr;
		{
			head.fnCenterHead;
			face.fnSmile;
			(0.5).wait;
			head.fnNod;
			this.fnSpeak(speech.fnGetGreat);
			(1.5).wait;
			this.fnSpeak(speech.fnGetLeadIn(stopCtr));
			3.wait;
			head.fnLeanForwards;
			face.fnInquisitive;
		}.fork;
	}

	fnStopped {arg data;
		{
			this.fnSpeak(speech.fnGetStopped(data));
			head.fnCenterHead;
			face.fnSmile;
			4.wait;
			head.fnLeanForwards;
			face.fnInquisitive;
		}.fork;
	}

	fnGoodbye {arg takenPhoto=false;
		{
			head.fnCenterHead;
			face.fnSmile;
			0.5.wait;
			this.fnSpeak(speech.fnGetGoodbye(takenPhoto));
		}.fork;
	}

	fnAreYouSure {
		{
			head.fnCenterHead;
			face.fnSad;
			1.wait;
			this.fnSpeak(speech.fnGetAreYouSure);
		}.fork;
	}

	fnWait {
		head.fnCenterHead;
		this.fnSpeak(speech.fnGetWait);
	}

	fnIntro {arg completion,remindAboutFB=false;

		this.fnSpeak(speech.fnGetIntro(remindAboutFB));

		Speech.doneAction_({
			completion.value;
			Speech.doneAction_({face.fnCloseMouth(true);isTalking=false});
		});
	}

	fnHow {arg firstTime=false;
		{
			head.fnCenterHead;
			face.fnSmile;
			(0.5).wait;
			this.fnSpeak(speech.fnGetGreat);
			head.fnNod;
			(1.5).wait;
			this.fnSpeak(speech.fnGetHow(firstTime));
			head.fnLeanForwards;
			face.fnInquisitive;
		}.fork;
	}

	fnChange {
		{
			head.fnCenterHead;
			face.fnSmile;
			(0.5).wait;
			this.fnSpeak(speech.fnGetGreat);
			head.fnNod;
			(1.5).wait;
			this.fnSpeak(speech.fnGetChange);
			head.fnLeanForwards;
			face.fnInquisitive;
		}.fork;
	}

	fnInvite {
		{
			head.fnCenterHead;
			face.fnSmile;
			(0.5).wait;
			this.fnSpeak(speech.fnGetInvite);
			(1).wait;
			head.fnLeanForwards;
			face.fnInquisitive;
		}.fork;
	}

	fnComeBack {
		this.fnSpeak(speech.fnGetComeBack++speech.fnGetInvite);
	}

	//MUSIC

	fnStartOrnament {
		var pick,facePick;
		pick=[0,1,2,3,4].choose;
		switch(pick,
			0,{face.fnTension},
			1,{
				head.fnLookToSideAndDown;
				facePick=[0,1,2,3].choose;
				switch(facePick,
					0,{face.fnTension},
					1,{face.fnElevated},
					2,{face.fnFrown},
					3,{}
				);},
			2,{
				{
					(1+0.5.rand).wait;
					face.fnRaiseEyebrows(0.1+0.2.rand);
				}.fork;
			},
			3,{
				{
					(1+0.5.rand).wait;
					face.fnExclamation(0.3+0.2.rand);
				}.fork;
			},
			4,{
				head.fnShake;
				facePick=[0,1,2].choose;
				switch(facePick,
					0,{face.fnTension},
					1,{face.fnElevated},
					2,{}
				);
			}
		);
	}

	fnEndOrnament {
		face.fnSmile;
		head.fnCenterHead;
	}

	fnStartBreakdown {
		var pick,facePick;
		pick=[0,1,2].choose;
		{
			1.wait;
			switch(pick,
				0,{
					face.fnTension;
					if(1.0.coin, {
						head.fnLookToSideAndDown;
					});
				},
				1,{
					face.fnElevated;
					if(0.75.coin, {
						head.fnLeanBack;
					},{
						head.fnLookToSideAndDown;
					});
				},
				2,{
					head.fnShake;
					facePick=[0,1,2,3].wchoose([0.2,0.1,0.3,0.4]);
					switch(facePick,
						0,{},
						1,{face.fnSmile},
						2,{face.fnElevated},
						3,{}
					);
				}
			);
		}.fork;
	}

	fnEndBreakdown {
		face.fnSmile;
		head.fnCenterHead;
	}

}