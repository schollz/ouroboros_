(
s.options.outDevice = "Speaker/HP (Realtek High Defini";
s.waitForBoot{
	"ready".postln;
	o=Ouroborus.new(Server.default,0);
	s.sync;
	o.recPrime(1,60/90*4,0.001);
})
o.setParam(1,"rate",1);
o.setParam(1,"bpm_target",90);
o.setParam(1,"bpm_sample",90);

o.recPrime(2,60/90*8,0.2);
o.recPrime(4,60/90*8,0.2);
o.recPrime(3,60/90*16,0.2);
o.setParam(3,"amp",-1);

SystemClock.seconds
o.recPrime(2,2,0.2);
o.recPrime(3,8,0.2);
o.setParam(3,"timestretch",0)

o.recStart(2,2,0.2);
o.recStart(3,8,0.2);

o.play(1,0.0,0,1.0);
o.play(2,0.0,0,1.0);
o.play(3,0.0,0,1.0);

(s.waitForBoot{
	"ready".postln;
	m=OuroborusPlay.new(Server.default,0);
	n=OuroborusPlay.new(Server.default,0);
b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav"); // remember to free the buffer later.
})
m.play(b,0.0,0.0,1.0);
n.play(b,0.0,0.0,1.0);
m.mapBus.at(\bitcrush).set(0)
n.mapBus.at(\timestretch).set(1)
n.mapBus.at(\amp).set(-60)


(s.waitForBoot{
	"ready".postln;
	m=OuroborusRec.new(Server.default,0,{ arg buf,startTime; [buf,startTime].postln; });
})

(
~startTime=SystemClock.seconds;
m.recStart(8.0,0.2);
)
(SystemClock.seconds-~startTime).mod(8)/8

m.recPrime(4.0,0.2);

(a={
	PlayBuf.ar(2,m.bufLoop,loop:1);
}.play)
a.free;

(1..2).do({ arg i; i.postln})

m.bufLoop.plot

Phasor
s
s.SystemClock.seconds
Date.getDate.second

SystemClock.seconds
s.seconds

Clock.seconds


c=Bus.control(s,1)
c.value=2;
c.set(5);
c.get({arg val; val.postln;})

({
	var imp=Dust.kr(4);
	EnvGen.ar(Env.new([0,1],[0.2],-2))*EnvGen.ar(Env.new([1,0],[0.2],-2),imp,doneAction:2)
}.plot(1)
)