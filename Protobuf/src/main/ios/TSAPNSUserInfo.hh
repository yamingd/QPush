#import <Foundation/Foundation.h>
#import "PBObjcWrapper.hh"

@interface TSAPNSUserInfo : NSObject<PBObjcWrapper>

@property (nonatomic,strong) NSString* key;
@property (nonatomic,strong) NSString* value;

@property (readonly,nonatomic,strong) NSData *protocolData;

@end