package org.acme.users.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Game {
    public Long id;
    public String userId;
    public Integer won;
    public Integer lost;
    public Boolean over;
    public Date created;
}
