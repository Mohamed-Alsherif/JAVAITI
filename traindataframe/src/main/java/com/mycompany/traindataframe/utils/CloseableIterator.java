package com.mycompany.traindataframe.utils;

import java.util.Iterator;

public interface CloseableIterator<E> extends Iterator<E>, AutoCloseable {

}
