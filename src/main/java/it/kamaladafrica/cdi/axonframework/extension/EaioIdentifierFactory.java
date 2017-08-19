package it.kamaladafrica.cdi.axonframework.extension;

import org.axonframework.common.IdentifierFactory;

import com.eaio.uuid.UUID;

public class EaioIdentifierFactory extends IdentifierFactory {

	@Override
	public String generateIdentifier() {
		return new UUID().toString();
	}

}
