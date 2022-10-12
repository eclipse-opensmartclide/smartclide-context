package org.eclipse.opensmartclide.pm2.common.application;

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
public interface SysCaller {

    String getLastError();

    String openDRMobject(String sObjName, String sPath, String sPerms);

    boolean closeDRMobject(String sHandle);

    byte[] readDRMobject(String sHandle, String path);

    byte[] readDRMobject2(String handle, String path, int count, int offset);

    boolean writeDRMobject(String sHandle, byte[] buf);

    byte[] getDRMobject(String sName, String path);

    boolean putDRMobject(String sName, String path, byte[] buf);

}
