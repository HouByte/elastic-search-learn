package cn.flowboot.es.doc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Good {
    private String title;
    private String imgUrl;
    private Double price;

}
