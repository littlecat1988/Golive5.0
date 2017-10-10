package com.golive.cinema.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class SerializerHelper<T extends Serializable> {

    /**
     * 写对象到文件
     */
    public boolean saveObject(T object, String path) {
        OutputStream os = null;
        ObjectOutput output = null;
        try {
            OutputStream file = new FileOutputStream(path);
            os = new BufferedOutputStream(file);
            output = new ObjectOutputStream(os);
            output.writeObject(object);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 从文件读取对象
     */
    public T loadObject(String path) {
        T object = null;
        InputStream is = null;
        ObjectInput input = null;
        try {
            InputStream file = new FileInputStream(path);
            is = new BufferedInputStream(file);
            input = new ObjectInputStream(is);
            Object readObject = input.readObject();
            object = (T) readObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }
}
