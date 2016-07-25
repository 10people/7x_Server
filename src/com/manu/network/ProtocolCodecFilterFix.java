package com.manu.network;

import java.net.SocketAddress;
import java.util.Queue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.NothingWrittenException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.apache.mina.filter.codec.AbstractProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolCodecFilterFix extends ProtocolCodecFilter{
	public static Logger log = LoggerFactory.getLogger(ProtocolCodecFilter.class.getSimpleName());
	public ProtocolEncoder en;
	public ProtocolDecoder de;
	public ProtocolCodecFilterFix(ProtocolEncoder encoder,
			ProtocolDecoder decoder) {
		super(encoder, decoder);
		en = encoder;
	}

	@Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object message = writeRequest.getMessage();

        // Bypass the encoding if the message is contained in a IoBuffer,
        // as it has already been encoded before
        if ((message instanceof IoBuffer) || (message instanceof FileRegion)) {
            nextFilter.filterWrite(session, writeRequest);
            return;
        }

        // Get the encoder in the session
        ProtocolEncoder encoder = en;

        ProtocolEncoderOutput encoderOut = getEncoderOut(session, nextFilter, writeRequest);

        if (encoder == null) {
            throw new ProtocolEncoderException("The encoder is null for the session " + session);
        }

        if (encoderOut == null) {
            throw new ProtocolEncoderException("The encoderOut is null for the session " + session);
        }

        try {
            // Now we can try to encode the response
            encoder.encode(session, message, encoderOut);

            // Send it directly
            Queue<Object> bufferQueue = ((AbstractProtocolEncoderOutput) encoderOut).getMessageQueue();

            // Write all the encoded messages now
            while (!bufferQueue.isEmpty()) {
                Object encodedMessage = bufferQueue.poll();

                if (encodedMessage == null) {
                    break;
                }

                // Flush only when the buffer has remaining.
                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
                    SocketAddress destination = writeRequest.getDestination();
                    WriteRequest encodedWriteRequest = new EncodedWriteRequest(encodedMessage, null, destination);

                    nextFilter.filterWrite(session, encodedWriteRequest);
                }
            }

            // Call the next filter
            nextFilter.filterWrite(session, new MessageWriteRequest(writeRequest));
        } catch (Throwable t) {
            ProtocolEncoderException pee;

            // Generate the correct exception
            if (t instanceof ProtocolEncoderException) {
                pee = (ProtocolEncoderException) t;
            } else {
                pee = new ProtocolEncoderException(t);
            }

            throw pee;
        }
    }
	 public final AttributeKey ENCODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "encoderOut");
	public ProtocolEncoderOutput getEncoderOut(IoSession session, NextFilter nextFilter, WriteRequest writeRequest) {
        ProtocolEncoderOutput out = (ProtocolEncoderOutput) session.getAttribute(ENCODER_OUT);

        if (out == null) {
            // Create a new instance, and stores it into the session
            out = new ProtocolEncoderOutputImpl(session, nextFilter, writeRequest);
            session.setAttribute(ENCODER_OUT, out);
        }

        return out;
    }
	public static class ProtocolEncoderOutputImpl extends AbstractProtocolEncoderOutput {
        public final IoSession session;

        public final NextFilter nextFilter;

        /** The WriteRequest destination */
        public final SocketAddress destination;

        public ProtocolEncoderOutputImpl(IoSession session, NextFilter nextFilter, WriteRequest writeRequest) {
            this.session = session;
            this.nextFilter = nextFilter;

            // Only store the destination, not the full WriteRequest.
            destination = writeRequest.getDestination();
        }

        public WriteFuture flush() {
            Queue<Object> bufferQueue = getMessageQueue();
            WriteFuture future = null;

            while (!bufferQueue.isEmpty()) {
                Object encodedMessage = bufferQueue.poll();

                if (encodedMessage == null) {
                    break;
                }

                // Flush only when the buffer has remaining.
                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
                    future = new DefaultWriteFuture(session);
                    nextFilter.filterWrite(session, new EncodedWriteRequest(encodedMessage, future, destination));
                }
            }

            if (future == null) {//不做处理 2015年9月25日11:18:26
                // Creates an empty writeRequest containing the destination
//                WriteRequest writeRequest = new DefaultWriteRequest(null, null, destination);
//                future = DefaultWriteFuture.newNotWrittenFuture(session, new NothingWrittenException(writeRequest));
//            	log.info("ok fix one, sid {}", session.getId());
            }

            return future;
        }
    }
	public static class EncodedWriteRequest extends DefaultWriteRequest {
        public EncodedWriteRequest(Object encodedMessage, WriteFuture future, SocketAddress destination) {
            super(encodedMessage, future, destination);
        }

        public boolean isEncoded() {
            return true;
        }
    }

    public static class MessageWriteRequest extends WriteRequestWrapper {
        public MessageWriteRequest(WriteRequest writeRequest) {
            super(writeRequest);
        }

        @Override
        public Object getMessage() {
            return EMPTY_BUFFER;
        }

        @Override
        public String toString() {
            return "MessageWriteRequest, parent : " + super.toString();
        }
    }
    public static final IoBuffer EMPTY_BUFFER = IoBuffer.wrap(new byte[0]);
    @Override
    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (writeRequest instanceof EncodedWriteRequest) {
            return;
        }

        if (writeRequest instanceof MessageWriteRequest) {
            MessageWriteRequest wrappedRequest = (MessageWriteRequest) writeRequest;
            nextFilter.messageSent(session, wrappedRequest.getParentRequest());
        } else {
            nextFilter.messageSent(session, writeRequest);
        }
    }
}
