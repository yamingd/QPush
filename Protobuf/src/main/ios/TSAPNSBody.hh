#import <Foundation/Foundation.h>
#import "PBObjcWrapper.hh"

@interface TSAPNSBody : NSObject<PBObjcWrapper>

@property (nonatomic,strong) NSString* alert;
@property (nonatomic,strong) NSString* sound;
@property (nonatomic,strong) NSNumber* badge;
@property (readonly,nonatomic,strong) NSData *protocolData;

@end