FoxDot
{

	classvar server;
	classvar midi;

	*start
	{

		server = Server.default;

		server.options.memSize = 8192 * 16; // increase this if you get "alloc failed" messages
		server.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message "too many nodes"
		server.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary
		server.options.numInputBusChannels = 2; // set this to your hardware output channel size, if necessary
		server.boot();

		MIDIClient.init;

		midi = MIDIOut(0);

		OSCFunc(
			{
				arg msg, time, addr, port;
				var note, vel, sus, channel;

				// listen for specific MIDI trigger messages from FoxDot

				msg.postln;

				note    = msg[1];
				vel     = msg[2];
				sus     = msg[3];
				channel = msg[4];

				Task.new(
					{
						midi.noteOn(channel, note, vel);
						sus.wait;
						midi.noteOff(channel, note, vel)

					}
				).play;

			},
			'foxdot_midi'

		);

		OSCFunc(
			{
				arg msg, time, addr, port;
				var fn, load;

				// Get local filename

				fn = msg[1].asString;

				// Print a message to the user

				fn.postln;

				// Add SynthDef to file

				fn = File(fn, "r");
				fn.readAllString.interpret;
				fn.close;

			},
			'foxdot'
		);

		StageLimiter.activate(2);

		"Listening for messages from FoxDot".postln;
	}
}