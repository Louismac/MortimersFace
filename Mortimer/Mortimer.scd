

//Set up Head+Face
(
//IP Of iPad here
d = "127.0.0.1";

c = ();
c[\processing] = NetAddr(d,23456);
c[\shouldTalk] = true;
c[\robot]=true;
c[\shouldMoveHead]=true;

b = NDrumBody.new(c);
)

//Test Face

b.face.fnTension;

b.face.fnSmile;

//Test Head

b.head.fnShake;

b.head.fnLeanForwards;

b.head.fnCenterHead;
