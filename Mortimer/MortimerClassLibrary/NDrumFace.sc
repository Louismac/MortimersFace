NDrumFace {

	var processing;
	var shouldAnimateFace;
	var eyelids,eyebrows,mouth;
	var blinkRoutine,isBlinking;
	var current,returnTo;
	var isChanging,expressionRoutine;
	var log;

	*new {arg inUse,p;
		^super.new.initNDrumFace(inUse,p);
	}

	initNDrumFace {arg inUse=true,p=nil;

		if(p==nil, {
			processing=NetAddr("127.0.0.1",23456);
			["connecting to Processing"].postln;
		},{
			processing=p;
		});

		if(inUse==nil,{inUse=true});

		shouldAnimateFace=inUse;

		eyelids=Array.fill(4,{arg i;i.asString});
		eyebrows=Array.fill(8,{arg i;i.asString});
		mouth=Array.fill(8,{arg i;i.asString});

		isChanging=false;
		isBlinking=false;

		current=();
		this.fnSet("eyebrow_l",3);
		this.fnSet("eyebrow_r",3);
		this.fnSet("mouth",0);
		this.fnSet("eyelid_l",0);
		this.fnSet("eyelid_r",0);

		returnTo=();
		current.keysValuesDo({arg key,value;
			returnTo[key]=value;
		});

		this.fnStartBlinking;

		expressionRoutine=List.new(0);

		this.fnResetLog;
	}

	fnGetLog {
		^log;
	}

	fnResetLog {
		log=List.new(0);
	}

	fnLog {arg event;
		log.add(event++" "++Date.getDate.stamp);
	}

	fnSet{arg feature, ptr,override=false;
		var set;

		if(override===nil,{override=false});

		switch(feature,
			"eyelid_r",{set=eyelids},
			"eyelid_l",{set=eyelids},
			"mouth",{set=mouth},
			"eyebrow_l",{set=eyebrows},
			"eyebrow_r",{set=eyebrows}
		);

		//["face",override].postln;

		override=false;

		if((shouldAnimateFace) || (override), {
			//[feature,set[ptr]].postln;
			if(set[ptr]!=nil, {
				processing.sendMsg(\nDrummer, feature, set[ptr]);
			});
		});
		if(set[ptr]!=nil, {current[feature.asSymbol]=ptr});
	}

	fnChangeExpression {arg target,step,restartBlinking=true,override=false;

		var dir,numSteps;

		dir=();
		numSteps=0;

		target.keysValuesDo({arg key,value;
			var dist;
			dist=(value-current[key]);
			if(dist!=0,{
				dir[key]=dist/dist.abs;
				if(dist.abs>numSteps,{numSteps=dist.abs});
			},{
				dir[key]=0;
			});
		});

		if(numSteps<1,{^"No face change specified, already in expression"});

		expressionRoutine.do{arg r;r.stop};
		expressionRoutine=List.new(0);

		//If you are blinking, stop for the duration of the transition
		if(((dir[\eyelid_l]!=0) || (dir[\eyelid_r]!=0)) && (isBlinking), {
			this.fnStopBlinking;
		});

		isChanging=true;

		expressionRoutine.add(
			{1.do{
				expressionRoutine.add(
					{numSteps.do{arg i;
						target.keysValuesDo({arg key,value;
							if(current[key]!=value,{
								this.fnSet(key.asString,current[key]+dir[key],override);
							});
						});
						step.wait;
					}}.fork;
				);
				(step*numSteps).wait;
				//if you should restart blinking, start again
				if(restartBlinking,{
					this.fnStartBlinking;
				});
				isChanging=false;
			}}.fork;
		);

	}

	fnStartBlinking {
		var blinkGap;

		if(isBlinking,{
			^"Already blinking";
		});

		isBlinking=true;
		if(blinkRoutine==nil, {
			//"starting blink routine".postln;
			blinkRoutine={inf.do{
				blinkGap=(1+(4.0.rand));
				blinkGap.wait;
				this.fnDoBlink;
			}}.fork;
		});
	}

	fnStopBlinking {
		//"stopping blink routine".postln;
		blinkRoutine.stop;
		blinkRoutine=nil;
		isBlinking=false;
	}

	fnDoBlink {
		var step,numLids;
		var slowerLid,fasterLid;

		if((current[\eyelid_l]==0) && (current[\eyelid_r]==0),{

			step=0.03;

			numLids=eyelids.size;

			if(0.5.coin,{
				slowerLid="eyelid_r";
				fasterLid="eyelid_l"
			},{
				slowerLid="eyelid_l";
				fasterLid="eyelid_r"
			});

			{1.do{

				//Down
				{numLids.do{arg ptr;
					this.fnSet(fasterLid,ptr);
					if(ptr<(numLids-1),{
						this.fnSet(slowerLid,(ptr-1).max(0));
					},{
						this.fnSet(slowerLid,ptr);
					});
					step.wait;
				}}.fork;

				(numLids*step).wait;

				//...And Up
				{numLids.do{arg ptr;
					this.fnSet(fasterLid,numLids-ptr-1);
					if(ptr<(numLids-1),{
						this.fnSet(slowerLid,(numLids-ptr).min(numLids-1));
					},{
						this.fnSet(slowerLid,numLids-ptr-1);
					});
					step.wait;
				}}.fork;

				(numLids*step).wait;

				//Vary frame length
				step=0.025+(0.01).rand;

			}}.fork;

		});
	}

	fnRaiseEyebrows {arg length;
		var target,step;

		target=();

		{
			target[\eyebrow_l]=0;
			target[\eyebrow_r]=0;
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=current[\mouth];

			if(isChanging==false,{
				returnTo[\eyebrow_l]=current[\eyebrow_l];
				returnTo[\eyebrow_r]=current[\eyebrow_r];
			});

			step=(length/2)/[current[\eyebrow_l],current[\eyebrow_r]].maxItem;

			this.fnChangeExpression(target, step);

			(length/2).wait;

			target[\eyebrow_l]=returnTo[\eyebrow_l];
			target[\eyebrow_r]=returnTo[\eyebrow_r];
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=current[\mouth];

			step=(length/2)/[returnTo[\eyebrow_l],returnTo[\eyebrow_r]].maxItem;

			this.fnChangeExpression(target, step);

		}.fork;

		this.fnLog("raise_eyebrows");
	}

	fnExclamation {arg length;
		var target,step;

		target=();

		{
			target[\eyebrow_l]=0;
			target[\eyebrow_r]=0;
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=7;

			if(isChanging==false,{
				returnTo[\eyebrow_l]=current[\eyebrow_l];
				returnTo[\eyebrow_r]=current[\eyebrow_r];
				returnTo[\mouth]=current[\mouth];
			});
			step=(length/2)/[current[\eyebrow_l],current[\eyebrow_r],7-(current[\mouth])].maxItem;

			this.fnChangeExpression(target, step);

			(length/2).wait;

			target[\eyebrow_l]=returnTo[\eyebrow_l];
			target[\eyebrow_r]=returnTo[\eyebrow_r];
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=returnTo[\mouth];

			this.fnChangeExpression(target, step);

		}.fork;

		this.fnLog("exclamation");

	}


	fnOpenMouth {arg forSpeech=false;
		var target;

		//If forSpeech is true then shouldAnimateFace is overridden

		if(current[\mouth]!=7,{
			target=();
			target[\eyebrow_l]=current[\eyebrow_l];
			target[\eyebrow_r]=current[\eyebrow_r];
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=7;

			if(isChanging==false,{returnTo[\mouth]=current[\mouth]});

			this.fnChangeExpression(target, 0.016);
		});
	}

	fnCloseMouth {arg forSpeech=false;
		var target;

		//If forSpeech is true then shouldAnimateFace is overridden

		if(current[\mouth]!=0,{

			target=();
			target[\eyebrow_l]=current[\eyebrow_l];
			target[\eyebrow_r]=current[\eyebrow_r];
			target[\eyelid_l]=current[\eyelid_l];
			target[\eyelid_r]=current[\eyelid_r];
			target[\mouth]=returnTo[\mouth];

			this.fnChangeExpression(target, 0.0135);
		});
	}

	fnCloseEyes {
		var target;

		target=();
		target[\eyebrow_l]=current[\eyebrow_l];
		target[\eyebrow_r]=current[\eyebrow_r];
		target[\eyelid_l]=3;
		target[\eyelid_r]=3;
		target[\mouth]=current[\mouth];

		this.fnChangeExpression(target, 0.075,false);
	}

	fnTension {
		var target;

		target=();
		target[\eyebrow_l]=7;
		target[\eyebrow_r]=6;
		target[\eyelid_l]=3;
		target[\eyelid_r]=3;
		target[\mouth]=3;

		this.fnChangeExpression(target, 0.075,false);

		this.fnLog("tension");
	}

	fnInquisitive {
		var target;

		"Changing face to inquisitive".postln;

		target=();
		target[\eyebrow_l]=0;
		target[\eyebrow_r]=0;
		target[\eyelid_l]=0;
		target[\eyelid_r]=0;
		target[\mouth]=0;

		this.fnChangeExpression(target, 0.075);

		this.fnLog("inquisitive");
	}

	fnFrown {
		var target;

		target=();
		target[\eyebrow_l]=7;
		target[\eyebrow_r]=6;
		target[\eyelid_l]=0;
		target[\eyelid_r]=0;
		target[\mouth]=3;

		this.fnChangeExpression(target, 0.075);

		this.fnLog("frown");

	}

	fnFrownBrows {
		var target;

		target=();
		target[\eyebrow_l]=6;
		target[\eyebrow_r]=6;
		target[\eyelid_l]=0;
		target[\eyelid_r]=0;
		target[\mouth]=current[\mouth];

		this.fnChangeExpression(target, 0.075);

		this.fnLog("frownbrows");
	}

	fnSad {

		var target;

		target=();
		target[\eyebrow_l]=0;
		target[\eyebrow_r]=0;
		target[\eyelid_l]=0;
		target[\eyelid_r]=0;
		target[\mouth]=3;

		this.fnChangeExpression(target, 0.075);

		this.fnLog("sad");
	}

	fnElevated {
		var target;

		target=();
		target[\eyebrow_l]=1;
		target[\eyebrow_r]=0;
		target[\eyelid_l]=3;
		target[\eyelid_r]=3;
		target[\mouth]=2;

		this.fnChangeExpression(target, 0.075,false);

		this.fnLog("elevated");
	}

	fnSmile {
		var target;

		target=();
		target[\eyebrow_l]=3;
		target[\eyebrow_r]=3;
		target[\eyelid_l]=0;
		target[\eyelid_r]=0;
		target[\mouth]=0;

		this.fnChangeExpression(target, 0.075);

		this.fnLog("smile");
	}

}