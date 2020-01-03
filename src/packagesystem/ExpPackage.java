/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package packagesystem;


/**
 *
 * @author frsy
 */
public class ExpPackage {
    public String pkgId;
    public String sCode;
    public String position;
    public String inDateTime;
    public String outDateTime;
    public String userNum;

    public ExpPackage(String pkgId, String sCode, String position, String inDateTime, String outDateTime, String userNum) {
        this.pkgId = pkgId;
        this.sCode = sCode;
        this.position = position;
        this.inDateTime = inDateTime;
        this.outDateTime = outDateTime;
        this.userNum = userNum;
    }
    
    
}
