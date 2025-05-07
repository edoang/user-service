package org.acme.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealRequest {
    public Long partyMemberId;
    public Long gameId;
    public Boolean healAll;

}
