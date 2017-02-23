NDrumHead {
var port,board;
var start,currentPos;
var id;
var shouldMoveHead;
var randomRoutine,shakeRoutine,nodRoutine;
var log;
var tempo;

	*new {arg p,b,inUse;
		^super.new.initNDrumHead(p,b,inUse);
	}

	initNDrumHead {arg p,b,inUse=true;

		start=();
		start[\tilt]=620;
		start[\pan]=340;

		currentPos=();
		currentPos[\tilt]=start[\tilt];
		currentPos[\pan]=start[\pan];

		id=();
		id[\pan]=1;
		id[\tilt]=2;

		this.fnResetLog;

		if(inUse==nil,{inUse=true});

		shouldMoveHead=inUse;

		if(shouldMoveHead,{
			{
				board=nil;
				port=p;
				if(port==nil,{
					port=this.fnFindPort;
				});

				["found port in NDrumHead", port].postln;

				if(b!=nil,{board=b},{
					if(port!=nil, {
						["connecting to board in NDrumHead",port].postln;
						try{
							board=SerialPort(port,
								baudrate: 115200,
								crtscts: true,
								xonxoff: false);
						} {
							arg error; [error,"board not found, try other usb port?"].postln;
						}
					});
				});

				3.wait;

				this.fnMoveServo(\pan,start[\pan],100);
				this.fnMoveServo(\tilt,start[\tilt],100);

			}.fork;
		});
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

	fnSetTempo {arg t;
		tempo=t;
	}

	fnMoveServo {arg motor,pos,speed;

		var motorId;

		motorId=id[motor];

		//["move",motor,pos,speed].postln;

		//Constrain within bounds (don't smash the head)
		if(motor==\pan,{
			pos=pos.clip(start[\pan]-200,start[\pan]+200);
			currentPos[\pan]=pos;
		},{
			pos=pos.clip(start[\tilt]-50,start[\tilt]+100);
			currentPos[\tilt]=pos;
		});

		if((board!=nil) && (shouldMoveHead), {
			board.putAll("servo");
			board.put(Char.nl);
			board.putAll(motorId.asString);
			board.put(Char.nl);
			board.putAll(pos.floor.asString);
			board.put(Char.nl);
			board.putAll(speed.floor.asString);
			board.put(Char.nl);
		});
	}

	fnStartRandom {
		randomRoutine = {inf.do{
			this.fnMoveWithSmoothOut(\pan,start[\pan]-200+(400.rand),1.5);
			if(0.5.coin,{
				this.fnMoveWithSmoothOut(\tilt,start[\tilt]+80,0.75);
			},{
				this.fnMoveWithSmoothOut(\tilt,start[\tilt],0.75);
			});
			(2+(3.rand)).wait;
		}}.fork;
	}

	fnStopRandom {
		randomRoutine.stop;
	}

	fnLookToSide {arg dir=nil;
		var panDir;
		if(dir==nil,{
			panDir= [-1,1].choose;
		},{
			panDir=dir;
		});
		this.fnMoveWithSmoothOut(\pan,start[\pan]+(150*(panDir)),1);

		this.fnLog("side_and_down");
	}

	fnLookToSideAndDown {arg dir=nil;
		var panDir;
		if(dir==nil,{
			panDir= [-1,1].choose;
		},{
			panDir=dir;
		});
		this.fnMoveWithSmoothOut(\tilt,start[\tilt]-80,0.75);
		this.fnMoveWithSmoothOut(\pan,start[\pan]+(150*(panDir)),1);

		this.fnLog("side_and_down");
	}

	fnCenterHead {
		shakeRoutine.stop;
		nodRoutine.stop;
		this.fnMoveWithSmoothOut(\tilt,start[\tilt],1.1);
		this.fnMoveWithSmoothOut(\pan,start[\pan],1.2);
	}

	fnLeanForwards {
		this.fnMoveWithSmoothOut(\tilt,start[\tilt]-80,0.75);

		this.fnLog("lean_forwards");
	}

	fnLeanBack {
		this.fnMoveWithSmoothOut(\tilt,start[\tilt]+50,0.75);

		this.fnLog("lean_back");
	}

	fnNod {
		nodRoutine = {
			this.fnMoveWithSmoothOut(\tilt,start[\tilt]-50,0.75);
			0.6.wait;
			this.fnMoveWithSmoothOut(\tilt,start[\tilt],0.8);
			0.5.wait;
		}.fork;

		this.fnLog("nod");
	}

	fnShake {arg temp=1;
		var m=1;
		m=temp;
		shakeRoutine = {inf.do{
			this.fnMoveWithSmoothOut(\pan,start[\pan]-100,1.2*m);
			(1/m).wait;
			this.fnMoveWithSmoothOut(\pan,start[\pan]+100,1.2*m);
			(1/m).wait;
		}}.fork;

		this.fnLog("shake");
	}

	fnMoveWithSmoothOut {arg motor, pos, sp;
		var dist=currentPos[motor]-pos;
		var mainGap=((0.0025*dist.abs)*sp);
		var stepGap=0.05*sp;
		var split=0.85;
		var steps=10;
		var stepSize=(dist*((1-split)/steps));

		{
			this.fnMoveServo(motor,currentPos[motor]-(dist*split),100*sp);
			mainGap.wait;
			{steps.do{arg i;
				this.fnMoveServo(motor,currentPos[motor]-stepSize,100*sp*((steps-(i/2))/steps));
				stepGap.wait;
			}}.fork;
		}.fork;
	}

	fnStep {arg dir;
		if(dir>0,{
			board.put(1);
		},{
			board.put(0);
		});
	}

	fnRead {
		board.next.postln;
	}

	fnFindPort {
		var ports;
		ports=SerialPort.devices;
		ports.do{arg p;
			if(p.contains("/dev/tty.usbmodem"),{
				^p
			});
		}
		^nil;
	}

}