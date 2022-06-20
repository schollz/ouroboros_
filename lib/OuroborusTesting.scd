(s.waitForBoot{
	"ready".postln;
	m=OuroborusRec(Server.default,0,{ arg buf,startTime; [buf,startTime].postln; });
})


m.recStart(2.0,0.2);

m.recPrime(4.0,0.2);

(a={
	PlayBuf.ar(2,m.bufLoop,loop:1);
}.play)
a.free;


m.bufLoop.plot

Phasor
s
s.SystemClock.seconds
Date.getDate.second

SystemClock.seconds
s.seconds

Clock.seconds