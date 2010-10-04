/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.skype.connector.ConnectorUtils;

public final class CSVPlayer extends Player {
    private final File file;
    private BufferedReader reader;
    
    public CSVPlayer(String filePath) throws IOException {
        this(new File(filePath));
    }

    public CSVPlayer(File file) throws IOException {
        ConnectorUtils.checkNotNull("file", file);
        this.file = file;
    }

    protected void init() {
        if (reader != null) {
            destory();
        }
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch(FileNotFoundException e) {
            throw new IllegalStateException("The CSV file is not found.", e);
        }
    }

    protected boolean hasNextMessage() {
        try {
            return reader.ready();
        } catch(IOException e) {
            return false;
        }
    }
    
    protected PlayerMessage getNextMessage() {
        String line = null;
        try {
            line = reader.readLine();
            String type = line.substring(0, line.indexOf(','));
            line = line.substring(line.indexOf(',') + 1);
            long time = Long.parseLong(line.substring(0, line.indexOf(',')));
            line = line.substring(line.indexOf(',') + 1);
            String message = line;
            if (type.equals("sent")) {
                return new PlayerMessage(PlayerMessage.Type.SENT, time, message);
            } else {
                return new PlayerMessage(PlayerMessage.Type.RECEIVED, time, message);
            }
        } catch(Exception e) {
            throw new IllegalStateException("A message couldn't be taken from the line (=\"" + line + "\").", e);
        }
    }

    protected void destory() {
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch(IOException e) {
            }
        }
    }
}
