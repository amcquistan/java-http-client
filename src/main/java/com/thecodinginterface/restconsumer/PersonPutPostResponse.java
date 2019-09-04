
package com.thecodinginterface.restconsumer;

import com.google.gson.annotations.SerializedName;

class PersonPutPostResponse {
    @SerializedName("data")
    private Person person;

    PersonPutPostResponse() {}

    PersonPutPostResponse(Person person) {
        this.person = person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    Person getPerson() {
        return person;
    }
}
