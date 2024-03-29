package org.eclipse.opensmartclide.context.services.faults;

/*
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.opensmartclide.context.services.faults.marshal.ContextFaultDetails;

import javax.xml.ws.WebFault;

/**
 * SelfLearningFault
 *
 * @author scholze
 * @version $LastChangedRevision: 417 $
 *
 */
@WebFault(faultBean = "org.eclipse.opensmartclide.context.services.faults.ContextFault", name = "ContextFault", targetNamespace = "http://atb-bremen.de")
public class ContextFault extends RuntimeException {
	private static final long serialVersionUID = 963073945982463366L;

	private ContextFaultDetails details;

	/**
	 * Constructs a new ContextFault with {@code null} as its detail
	 * message. The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 */
	public ContextFault() {
		super();
	}

	/**
	 * Constructs a new ContextFault with the specified detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call
	 * to {@link #initCause}.
	 *
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the {@link #getMessage()} method.
	 */
	public ContextFault(final String message) {
		this(message, new ContextFaultDetails(message), null);
	}

	/**
	 * Constructs a new ContextFault with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is
	 * <i>not</i> automatically incorporated in this exception's detail message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public ContextFault(final String message, final Throwable cause) {
		this(message, new ContextFaultDetails(message, cause), cause);
	}

	/**
	 * Constructs a new ContextFault with the specified cause and a detail
	 * message of <tt>(cause==null ? null : cause.toString())</tt> (which
	 * typically contains the class and detail message of <tt>cause</tt>). This
	 * constructor is useful for exceptions that are little more than wrappers
	 * for other throwables (for example,
	 * {@link java.security.PrivilegedActionException}).
	 *
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public ContextFault(final Throwable cause) {
		this(null, new ContextFaultDetails(cause), null);
	}

	public ContextFault(final String message, final ContextFaultDetails details) {
		this(message, details, null);
	}

	public ContextFault(final String message,
						final ContextFaultDetails details, final Throwable cause) {
		super(message, cause);
		this.details = details;
	}

	@Override
	public final String getMessage() {
		if (details != null) {
			return details.asThrowable().getMessage();
		} else {
			return super.getMessage();
		}
	}

	@Override
	public final Throwable getCause() {
		if (details != null) {
			return details.asThrowable();
		} else {
			return super.getCause();
		}
	}

	public final ContextFaultDetails getFaultInfo() {
		return details;
	}
}
