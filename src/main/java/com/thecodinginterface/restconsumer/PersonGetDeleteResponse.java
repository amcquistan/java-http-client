
package com.thecodinginterface.restconsumer;

import com.google.gson.annotations.SerializedName;

class PersonGetDeleteResponse {
    @SerializedName("args")
    private Person person;

    PersonGetDeleteResponse() {}

    PersonGetDeleteResponse(Person person) {
        this.person = person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    Person getPerson() {
        return person;
    }
}
