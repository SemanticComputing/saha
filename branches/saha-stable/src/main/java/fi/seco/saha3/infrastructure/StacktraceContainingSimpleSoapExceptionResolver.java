package fi.seco.saha3.infrastructure;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapMessage;

public class StacktraceContainingSimpleSoapExceptionResolver extends AbstractEndpointExceptionResolver {

	@Override
	protected final boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse(), "SimpleSoapExceptionResolver requires a SoapMessage");
		SoapMessage response = (SoapMessage) messageContext.getResponse();
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		response.getSoapBody().addServerOrReceiverFault(sw.toString(), Locale.ENGLISH);
		return true;
	}

}
