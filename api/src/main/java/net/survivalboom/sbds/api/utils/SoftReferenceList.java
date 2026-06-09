package net.survivalboom.sbds.api.utils;

import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class SoftReferenceList<E> extends AbstractList<E> implements List<E> {

    private static final int DEFAULT_CAPACITY = 10;

    // Внутренний массив заполнен мягкими ссылками
    private SoftReference<E>[] elementData;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public SoftReferenceList() {
        this.elementData = (SoftReference<E>[]) new SoftReference[DEFAULT_CAPACITY];
    }

    // 1. Получение элемента по индексу
    @Override
    public E get(int index) {
        checkIndex(index);
        SoftReference<E> ref = elementData[index];
        return ref != null ? ref.get() : null;
    }

    // 2. Текущий размер списка (количество зарезервированных слотов)
    @Override
    public int size() {
        return size;
    }

    // 3. Добавление элемента в конец списка
    @Override
    public boolean add(E element) {
        ensureCapacity();
        elementData[size++] = new SoftReference<>(element);
        modCount++; // Важно для правильной работы итераторов AbstractList
        return true;
    }

    // 4. Вставка элемента по индексу
    @Override
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity();
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = new SoftReference<>(element);
        size++;
        modCount++;
    }

    // 5. Замена элемента по индексу
    @Override
    public E set(int index, E element) {
        checkIndex(index);
        E oldValue = get(index);
        elementData[index] = new SoftReference<>(element);
        return oldValue;
    }

    // 6. Удаление элемента по индексу
    @Override
    public E remove(int index) {
        checkIndex(index);
        E oldValue = get(index);

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        }
        elementData[--size] = null; // зануляем последний элемент для GC
        modCount++;
        return oldValue;
    }

    // --- Вспомогательные методы ---

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void ensureCapacity() {
        if (size == elementData.length) {
            int newCapacity = elementData.length * 2;
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    /**
     * Специфичный метод для очистки массива от ссылок, 
     * объекты которых уже были стерты сборщиком мусора.
     */
    public void gcTrim() {
        int writeIndex = 0;
        for (int i = 0; i < size; i++) {
            if (elementData[i] != null && elementData[i].get() != null) {
                elementData[writeIndex++] = elementData[i];
            }
        }
        // Зануляем оставшиеся хвосты
        for (int i = writeIndex; i < size; i++) {
            elementData[i] = null;
        }
        if (size != writeIndex) {
            modCount++;
        }
        size = writeIndex;
    }
}