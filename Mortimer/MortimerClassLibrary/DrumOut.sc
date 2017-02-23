//Class to play a sequence

DrumOut {
	var port,board;
	var <>instruments,<>tempo,<>cs,maxLat;
	var mainSeq,clock,resp,noBot;
	var net,sendingOut=false;


	*new {arg p,b;
		^super.new.initDrumOut(p,b);
	}

	initDrumOut {arg p,b;

		if(sendingOut,{net=NetAddr.new("169.254.12.187", 12345)});

		port=p;
		if(port==nil,{
			port=this.fnFindPort;
		});

		["found port in DrumOut", port].postln;

		maxLat=0;
		tempo=100;
		clock=TempoClock(tempo/60);
		instruments=();
		noBot=Buffer.read(Server.default, Platform.userAppSupportDir ++ "/sounds/kick3.wav";);

		if(b!=nil,{board=b},{
			if(port!=nil, {
				try{
					["connecting to board in DrumOut",port].postln;
					board=SerialPort(port,
						baudrate: 115200,
						crtscts: true,
						xonxoff: false);
				} {
					arg error; [error,"board not found, try other usb port?"].postln;
				}
			});
		});
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

	addArm {arg key, pin,strikeLength,lat=0;
		instruments[key]=[pin,strikeLength,lat];
		if(lat>maxLat,{maxLat=lat});
	}

	getMaxLat{
		^maxLat;
	}

	getLat {arg key;
		^instruments[key][2];
	}

	setLat {arg key,val;
		instruments[key][2]=val;
		if(val>maxLat,{maxLat=val});
	}

	getSL {arg key;
		^instruments[key][1];
	}

	setSL {arg key,val;
		instruments[key][1]=val;
	}

	setTempo {arg t;
		tempo=t;
		clock.tempo_(t/60);
	}

	playSeq {arg seq,iterations=1,barLength=16,dur=0.25;
		var barWait;
		if(dur.size==0,{barWait=dur*barLength},{barWait=dur.sum});
		{iterations.do{
			{barLength.do{arg i;
				seq.keysValuesDo({arg key, value;
					if(seq[key][i].asString=="1", {
						this.noteOn(key);
					});
				});
				if(dur.size==0,{dur.wait},{dur[i].wait});
			}}.fork(clock);
			barWait.wait;
		}}.fork(clock);
	}

	noteOn{arg key;
		{1.do{
			if(sendingOut,{net.sendMsg(\drum,key.asString)});
			//["noteOn",key,this.getMaxLat-this.getLat(key)].postln;
			(this.getMaxLat-this.getLat(key)).wait;
			this.strike(key);
		}}.fork;
	}

	stop {
		mainSeq.stop;
	}

	read {
		board.next.postln;
	}

	send {arg input;
		board.put(input);
	}

	sendAll {arg input;
		board.putAll(input);
	}

	strike{arg key;
		{try{
			//["strike",instruments[key]].postln;
			board.putAll("solenoid");
			board.put(Char.nl);
			board.putAll(instruments[key][0].asString);
			board.put(Char.nl);
			board.putAll("HIGH");
			board.put(Char.nl);
			cs=1;
			instruments[key][1].wait;
			board.putAll("solenoid");
			board.put(Char.nl);
			board.putAll(instruments[key][0].asString);
			board.put(Char.nl);
			board.putAll("LOW");
			board.put(Char.nl);
			}{arg error;
				[error,"strike failed"].postln;
				Synth(\playBuf,[\bufNum,noBot]);
			}
		}.fork;
	}
}