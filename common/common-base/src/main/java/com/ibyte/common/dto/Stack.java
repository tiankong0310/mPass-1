package com.ibyte.common.dto;

import lombok.*;

/**
 * @Description: <Stack>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-09 21:25
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stack {

    String app;

    String code;

    String message;
}
