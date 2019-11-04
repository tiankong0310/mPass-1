package com.ibyte.common.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

/**
 * @Description: <Determine>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-09 21:26
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Determine {


    String name;

    HttpStatus status;
}
