package com.kelsos.mbrc.dto.requests

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder("from", "to")
class MoveRequest {

  constructor()

  constructor(from: Int, to: Int) {
    this.from = from
    this.to = to
  }

  @JsonProperty("from") var from: Int = 0
  @JsonProperty("to") var to: Int = 0

}
