package org.acme.users.model;

import lombok.Data;

@Data
public class Battle {
    public String userId;
    public Long partyMemberId;
    public Boolean won;

}
