package com.example.task1;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Human implements Externalizable {
    //-- Class members -------------------------------
    /**
     * Фамилия
     */
    public String firstName;
    /**
     * Имя
     */
    public String lastName;
    /**
     * Пол. true - мужской
     */
    public boolean gender;
    /**
     * День рождения
     */
    public Calendar birthDay;
    //-- Class methods -------------------------------
    public	Human(String firstName, String lastName,
                    boolean gender, Calendar birthDay)
    {
        this.firstName	 = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDay = birthDay;
    }

    public Human() {

    }

    /**
     * Возвращает дату рождения в виде строки dd/mm/yyyy
     */
    public String getBirthDayString()
    {
        String str = "";
        int day = this.birthDay.get(Calendar.DAY_OF_MONTH);
        str += ((day < 10)?"0":"") + day + "/";
        int mon = this.birthDay.get(Calendar.MONTH) + 1;
        str += ((mon < 10)?"0":"") + mon + "/";
        str += this.birthDay.get(Calendar.YEAR);
        return str;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(firstName);
        objectOutput.writeUTF(lastName);
        objectOutput.writeBoolean(gender);
        objectOutput.writeInt(birthDay.get(Calendar.YEAR));
        objectOutput.writeInt(birthDay.get(Calendar.MONTH));
        objectOutput.writeInt(birthDay.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException {
        firstName = objectInput.readUTF();
        lastName = objectInput.readUTF();
        gender = objectInput.readBoolean();
        int year = objectInput.readInt();
        int month = objectInput.readInt();
        int day = objectInput.readInt();
        birthDay = new GregorianCalendar(year, month, day);
    }
}
